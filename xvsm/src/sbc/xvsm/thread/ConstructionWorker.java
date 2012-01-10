package sbc.xvsm.thread;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.LoggerFactory;

import sbc.SbcConstants;
import sbc.dto.Computer;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class ConstructionWorker implements Runnable {

	private boolean running=true;
	private Capi capi;
	private MzsCore core;
	private ContainerReference productionContainer;
	private ContainerReference testContainer;
	private ContainerReference notficationContainer;
	//	private String workername;

	public static void main(String[] args){
		if(args.length!=1){
			System.err.println("Main Port has to be specified");
			System.exit(1);
		}
		SbcConstants.MAINPORT=Integer.parseInt(args[0]);
		
		ConstructionWorker constructor=new ConstructionWorker();
		Thread t = new Thread(constructor);
		t.start();
		System.out.println("Construction worker started");
	}

	public void run() {

		//		workername="tester"+new SecureRandom().nextLong();

		try{
			//Set up local mozart space
			LoggerContext context = (LoggerContext) LoggerFactory
			.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");

			try{
				core = DefaultMzsCore.newInstance(SbcConstants.MAINPORT+SbcConstants.CONSTRUCTIONPORTOFFSET);
				System.out.println(core.getConfig().getSpaceUri());
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

			//			FifoSelector fifoSelect=FifoCoordinator.newSelector();

			List<Selector> cpuSelectors=new ArrayList<Selector>();
			cpuSelectors.add(LindaCoordinator.newSelector(new CpuComponent(null,null,null)));
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

			System.out.println("Setup complete");
			while(running){

				CpuComponent cpuComponent=null;
				MainboardComponent mainboardComponent=null;
				ArrayList<RamComponent> ramComponents=new ArrayList<RamComponent>();
				GpuComponent gpuComponent=null;

				TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
				try{
					//First: Obtain obligatory parts (1 GPU, 1 Mainboard, 1 RAM)
					ArrayList<CpuComponent> cpuResultEntries = capi.take(productionContainer, cpuSelectors, MzsConstants.RequestTimeout.INFINITE, null);
					ArrayList<MainboardComponent> mainboardResultEntries = capi.take(productionContainer, mainboardSelectors, MzsConstants.RequestTimeout.INFINITE, null);
					ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectors, MzsConstants.RequestTimeout.INFINITE, null);
					cpuComponent=cpuResultEntries.get(0);
					mainboardComponent=mainboardResultEntries.get(0);
					ramComponents.add(ramResultEntries.get(0));
					capi.commitTransaction(tx);
					System.out.print("transaction committed");
				}catch(Exception ex){
					capi.rollbackTransaction(tx);
					System.out.println("Transaction rolled back");
					continue;	//restart loop
				}

				System.out.println("Got all obligatory components");

				tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
				try{	
					//Try to obtain optional part: GPU
					ArrayList<GpuComponent> gpuResultEntries = capi.take(productionContainer, gpuSelectors, MzsConstants.RequestTimeout.TRY_ONCE, null);
					gpuComponent=gpuResultEntries.get(0);
					capi.commitTransaction(tx);
				}catch(Exception ex){
					capi.rollbackTransaction(tx);
					System.out.println("GPU-Transaction rolled back");
				}
				tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
				try{	
					//Try to obtain optional part: second RAM
					ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectors, MzsConstants.RequestTimeout.TRY_ONCE, null);
					ramComponents.add(ramResultEntries.get(0));
					capi.commitTransaction(tx);
				}catch(Exception ex){
					capi.rollbackTransaction(tx);
					System.out.println("RAM-Transaction rolled back");
				}
				tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
				try{	
					//Try to obtain optional part: 2 more RAM modules
					ArrayList<RamComponent> ramResultEntries = capi.take(productionContainer, ramSelectorsGetTwoRams, MzsConstants.RequestTimeout.TRY_ONCE, null);
					ramComponents.add(ramResultEntries.get(0));
					ramComponents.add(ramResultEntries.get(1));
					capi.commitTransaction(tx);
				}catch(Exception ex){
					capi.rollbackTransaction(tx);
					System.out.println("2-RAM-Transaction rolled back");
				}

//				System.out.println(cpuComponent.getClass());
//				System.out.println(mainboardComponent.getClass());
//				System.out.println(ramComponents.get(0).getClass() +" "+ ramComponents.size());
//				if(gpuComponent!=null){
//					System.out.println(gpuComponent.getClass());
//				}

				Computer computer=new Computer(cpuComponent, mainboardComponent,gpuComponent,ramComponents);
				Entry e=new Entry(computer);
				capi.write(testContainer, e);
				capi.write(notficationContainer, new Entry("New computer constructed"));
				System.out.println("New computer constructed");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
}

