package sbc.xvsm.thread;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.LoggerFactory;

import sbc.SbcConstants;
import sbc.dto.Computer;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.RamComponent;
import sbc.job.Configuration;
import sbc.job.Job;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class ConstructionWorker implements Runnable {

	private boolean running = true;
	private Capi capi;
	private MzsCore core;
	private ContainerReference productionContainer;
	private ContainerReference testContainer;
	private ContainerReference notficationContainer;
	private ContainerReference jobContainer;

	// private String workername;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Main Port has to be specified");
			System.exit(1);
		}
		try {
			SbcConstants.MAINPORT = Integer.parseInt(args[0]);
			ConstructionWorker constructor = new ConstructionWorker();
			Thread t = new Thread(constructor);
			t.start();
			System.out.println("Construction worker started");
		} catch (NumberFormatException ex) {
			System.err
					.println("Given port argument is no number! XVSM not started!");
			ex.printStackTrace();
		}
	}

	public void run() {
		try{
			initXvsm();
			while(running){
				Job currentJob = nextJob();
				
				//if no job is available or fulfillable, build other computers
				if(currentJob == null){
					buildDefault();
				}
				else{
					while(currentJob.getQuantity()>0){
						boolean computerBuildt = buildForJob(currentJob);
						if(computerBuildt){
							currentJob.setQuantity(currentJob.getQuantity()-1);
						}
						else{
							suspendJob(currentJob);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initXvsm() throws Exception{
		//Set up local mozart space
		LoggerContext context = (LoggerContext) LoggerFactory
		.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure("logback.xml");

		try{
			core = DefaultMzsCore.newInstance(SbcConstants.MAINPORT+SbcConstants.CONSTRUCTIONPORTOFFSET);
			capi = new Capi(core);

			testContainer = capi.createContainer(
					SbcConstants.TESTERCONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new QueryCoordinator());
			
		}catch(MzsCoreRuntimeException ex){
			//There exits yet another tester, logisticContainer is already created
			core = DefaultMzsCore.newInstance(0);
			capi = new Capi(core);
			this.testContainer=capi.lookupContainer(SbcConstants.TESTERCONTAINER, new URI("xvsm://localhost:+"+(SbcConstants.MAINPORT+SbcConstants.CONSTRUCTIONPORTOFFSET)), MzsConstants.RequestTimeout.INFINITE, null);
		}
		System.out.println("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET));
		this.notficationContainer=capi.lookupContainer(SbcConstants.NOTIFICATIONCONTAINER, new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET)), MzsConstants.RequestTimeout.INFINITE, null);
		this.productionContainer=capi.lookupContainer(SbcConstants.PRODUCERCONTAINER, new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)), MzsConstants.RequestTimeout.INFINITE, null);
		this.jobContainer = capi.lookupContainer(SbcConstants.JOBSCONTAINER, new URI("xvsm://localhost:" + (SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)), MzsConstants.RequestTimeout.INFINITE, null);
		System.out.println("Setup complete");
		capi.write(notficationContainer, new Entry("ConstructionWorker: Setup complete, port: "+(SbcConstants.MAINPORT+SbcConstants.CONSTRUCTIONPORTOFFSET)));
	}
	
	private Job nextJob() throws MzsCoreException, URISyntaxException{
		
		List<Job> jobList = new ArrayList<Job>();
		
		TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)));
		try{
			ArrayList<Job> entryList = capi.read(this.jobContainer, FifoCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX), MzsConstants.RequestTimeout.TRY_ONCE, null);
			for(Job job: entryList){
				
				if(canFulfillJob(job)){
					return job;
				}
			}
			
			capi.commitTransaction(tx);
		}
		catch(Exception e){
			capi.rollbackTransaction(tx);
		}
		return null;
	}
	
	private void suspendJob(Job job) throws MzsCoreException, URISyntaxException{
		capi.write(this.jobContainer, new Entry(job)); 
	}
	
	private boolean canFulfillJob(Job job) throws MzsCoreException, URISyntaxException{
		TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)));

		Configuration config = job.getConfiguration();
		
		List<Selector> cpuSelectors=new ArrayList<Selector>();
		cpuSelectors.add(LindaCoordinator.newSelector(new CpuComponent(null,null,null, config.getCpuType())));
		cpuSelectors.add(FifoCoordinator.newSelector());

		List<Selector> mainboardSelectors=new ArrayList<Selector>();
		mainboardSelectors.add(LindaCoordinator.newSelector(new MainboardComponent(null,null,null)));
		mainboardSelectors.add(FifoCoordinator.newSelector());

		List<Selector> ramSelectors=new ArrayList<Selector>();
		ramSelectors.add(LindaCoordinator.newSelector(new RamComponent(null,null,null)));
		ramSelectors.add(FifoCoordinator.newSelector(config.getRamModuleCount()));

		List<Selector> gpuSelectors=new ArrayList<Selector>();
		gpuSelectors.add(LindaCoordinator.newSelector(new GpuComponent(null,null,null)));
		gpuSelectors.add(FifoCoordinator.newSelector());
		
		try{
			ArrayList<CpuComponent> cpuResultEntries = capi.read(productionContainer, cpuSelectors, MzsConstants.RequestTimeout.TRY_ONCE, tx);
			ArrayList<MainboardComponent> mainboardResultEntries = capi.read(productionContainer, mainboardSelectors, MzsConstants.RequestTimeout.TRY_ONCE, tx);
			ArrayList<RamComponent> ramResultEntries = capi.read(productionContainer, ramSelectors, MzsConstants.RequestTimeout.TRY_ONCE, tx);
			ArrayList<GpuComponent> gpuResultEntries = null;
			
			if(config.isGraphicsCard()){
				gpuResultEntries = capi.read(productionContainer, gpuSelectors, MzsConstants.RequestTimeout.TRY_ONCE, tx);
			}
			//if we could read all components, job can be started
			capi.commitTransaction(tx);
			return true;
		}
		catch(Exception e){
			capi.rollbackTransaction(tx);
			return false;
		}
	}
	
	private void buildDefault() throws MzsCoreException{
		List<Selector> cpuSelectors=new ArrayList<Selector>();
		cpuSelectors.add(LindaCoordinator.newSelector(new CpuComponent(null,null,null,null)));
		cpuSelectors.add(FifoCoordinator.newSelector());

		List<Selector> mainboardSelectors=new ArrayList<Selector>();
		mainboardSelectors.add(LindaCoordinator.newSelector(new MainboardComponent(null,null,null)));
		mainboardSelectors.add(FifoCoordinator.newSelector());

		List<Selector> ramSelectors=new ArrayList<Selector>();
		ramSelectors.add(LindaCoordinator.newSelector(new RamComponent(null,null,null)));
		ramSelectors.add(FifoCoordinator.newSelector());

		List<Selector> ramSelectorsGetTwoRams=new ArrayList<Selector>();
		ramSelectorsGetTwoRams.add(LindaCoordinator.newSelector(new RamComponent(null,null,null),2));
		ramSelectorsGetTwoRams.add(FifoCoordinator.newSelector(2));

		List<Selector> gpuSelectors=new ArrayList<Selector>();
		gpuSelectors.add(LindaCoordinator.newSelector(new GpuComponent(null,null,null)));
		gpuSelectors.add(FifoCoordinator.newSelector());
		
		CpuComponent cpuComponent=null;
		MainboardComponent mainboardComponent=null;
		ArrayList<RamComponent> ramComponents=new ArrayList<RamComponent>();
		GpuComponent gpuComponent=null;

		TransactionReference tx = null;
		try{
			tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, new URI("xvsm://localhost:" + (SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)));
			//First: Obtain obligatory parts (1 GPU, 1 Mainboard, 1 RAM)
			ArrayList<CpuComponent> cpuResultEntries = capi.take(productionContainer, cpuSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, tx);
			ArrayList<MainboardComponent> mainboardResultEntries = capi.take(productionContainer, mainboardSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, tx);
			ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, tx);
			cpuComponent=cpuResultEntries.get(0);
			mainboardComponent=mainboardResultEntries.get(0);
			ramComponents.add(ramResultEntries.get(0));
			capi.commitTransaction(tx);
			System.out.print("Got all obligatory parts");
		}catch(Exception ex){
			if(tx!=null){
				capi.rollbackTransaction(tx);
				System.out.println("Got not all parts, rolling back transaction");
				//computer cannot be constructed because basic parts are not available, so stopping
				return;
			}
		}

		System.out.println("Got all obligatory components");

		tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
		try{	
			//Try to obtain optional part: GPU
			ArrayList<GpuComponent> gpuResultEntries = capi.take(productionContainer, gpuSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
			gpuComponent=gpuResultEntries.get(0);
			capi.commitTransaction(tx);
		}catch(Exception ex){
			capi.rollbackTransaction(tx);
			System.out.println("GPU-Transaction rolled back");
		}
		tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
		try{	
			//Try to obtain optional part: second RAM	
			ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
			ramComponents.add(ramResultEntries.get(0));
			capi.commitTransaction(tx);
		}catch(Exception ex){
			capi.rollbackTransaction(tx);
			System.out.println("RAM-Transaction rolled back");
		}
		tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
		try{	
			//Try to obtain optional part: 2 more RAM modules
			ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectorsGetTwoRams, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
			ramComponents.add(ramResultEntries.get(0));
			ramComponents.add(ramResultEntries.get(1));
			capi.commitTransaction(tx);
		}catch(Exception ex){
			capi.rollbackTransaction(tx);
			System.out.println("2-RAM-Transaction rolled back");
		}
		
		Computer computer=new Computer(cpuComponent, mainboardComponent,gpuComponent,ramComponents);
		Entry e=new Entry(computer);
		capi.write(testContainer, e);
		capi.write(notficationContainer, new Entry("New computer constructed, no job"));
		System.out.println("New computer constructed, no job");
	}
	
	private boolean buildForJob(Job job) throws MzsCoreException, URISyntaxException{
		TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)));

		Configuration config = job.getConfiguration();
		
		List<Selector> cpuSelectors=new ArrayList<Selector>();
		cpuSelectors.add(LindaCoordinator.newSelector(new CpuComponent(null,null,null, config.getCpuType())));
		cpuSelectors.add(FifoCoordinator.newSelector());

		List<Selector> mainboardSelectors=new ArrayList<Selector>();
		mainboardSelectors.add(LindaCoordinator.newSelector(new MainboardComponent(null,null,null)));
		mainboardSelectors.add(FifoCoordinator.newSelector());

		List<Selector> ramSelectors=new ArrayList<Selector>();
		ramSelectors.add(LindaCoordinator.newSelector(new RamComponent(null,null,null)));
		ramSelectors.add(FifoCoordinator.newSelector(config.getRamModuleCount()));

		List<Selector> gpuSelectors=new ArrayList<Selector>();
		gpuSelectors.add(LindaCoordinator.newSelector(new GpuComponent(null,null,null)));
		gpuSelectors.add(FifoCoordinator.newSelector());
		
		CpuComponent cpuComponent = null;
		MainboardComponent mainboardComponent = null;
		List<RamComponent> ramComponents = new ArrayList<RamComponent>();
		GpuComponent gpuComponent = null;
		
		try{
			ArrayList<CpuComponent> cpuResultEntries = capi.take(productionContainer, cpuSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
			ArrayList<MainboardComponent> mainboardResultEntries = capi.take(productionContainer, mainboardSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
			ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
			cpuComponent = cpuResultEntries.get(0);
			mainboardComponent = mainboardResultEntries.get(0);
			
			if(config.isGraphicsCard()){
				ArrayList<GpuComponent> gpuResultEntries = capi.take(productionContainer, gpuSelectors, SbcConstants.CONSTRUCTION_TAKE_TIMEOUT, null);
				gpuComponent = gpuResultEntries.get(0);
			}
			capi.commitTransaction(tx);
			Computer computer=new Computer(cpuComponent, mainboardComponent,gpuComponent,ramResultEntries);
			Entry e=new Entry(computer);
			capi.write(testContainer, e);
			capi.write(notficationContainer, new Entry("New computer constructed, job uuid: " + job.getUuid()));
			System.out.println("New computer constructed, job uuid: " + job.getUuid());
			return true;
		}
		catch(Exception e){
			capi.rollbackTransaction(tx);
			e.printStackTrace();
			return false;
		}
	}
}
