package sbc.loadbalancing;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;
import org.mozartspaces.capi3.AnyCoordinator;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import sbc.SbcConstants;
import sbc.dto.ComponentEnum;
import sbc.dto.CpuComponent;
import sbc.dto.CpuComponent.CpuType;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.ProductionOrder;
import sbc.dto.RamComponent;
import sbc.job.Job;

public class LoadBalancer implements Runnable, NotificationListener {

	//List of all demanded components
	//	private List<ProductionOrder> orderList;

	private ContainerReference registrationContainer;
	private Capi capi;
	private MzsCore core;

	//TODO: Obtain reference of other fabrics
	private HashMap<Integer,FactoryReference> factories=new HashMap<Integer,FactoryReference>();

	private boolean running=true;

	public static void main(String args[]){
		LoadBalancer l=new LoadBalancer();
		Thread lb=new Thread(l);
		lb.start();
		System.out.println("Loadbalancer started");
	}
	

	/**
	 * Check regularly if components should be transfered
	 * Components are transfered, if a certain limit has been exceeded
	 * All needed information is extracted from the containers and
	 * the current list of orders.
	 */
	@Override
	public void run() {
		try{
			LoggerContext context = (LoggerContext) LoggerFactory
			.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");
			
			core = DefaultMzsCore.newInstance(SbcConstants.LoadBalancerPort);
			capi = new Capi(core);

			registrationContainer = capi.createContainer(
					SbcConstants.LOADBALANCERCONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new QueryCoordinator());
			NotificationManager notifManager = new NotificationManager(core);
			notifManager.createNotification(registrationContainer, this, Operation.WRITE);

			while(running){
				Thread.sleep(10000);
				checkOverallProductionStatus();
			}
		}catch (Exception e) {
			System.err.println("Error: Loadbalancer already running.");
			e.printStackTrace();
		}
	}

	private void checkOverallProductionStatus() {
		System.out.println("Checking overall status");

		LindaSelector cpuSelector=LindaCoordinator.newSelector(new CpuComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		LindaSelector mainboardSelector=LindaCoordinator.newSelector(new MainboardComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		LindaSelector ramSelector=LindaCoordinator.newSelector(new RamComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		LindaSelector gpuSelector=LindaCoordinator.newSelector(new GpuComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		FifoSelector jobsSelector=FifoCoordinator.newSelector(MzsConstants.Selecting.COUNT_MAX);

		for(FactoryReference factoryReference:factories.values()){
			ContainerReference productionContainer=factoryReference.getProductionContainer();
			ContainerReference jobContainer=factoryReference.getJobsContainer();
			try{
				ArrayList<CpuComponent> cpuComponents=capi.read(productionContainer, cpuSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<MainboardComponent> mainboardComponents=capi.read(productionContainer, mainboardSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<RamComponent> ramComponents=capi.read(productionContainer, ramSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<GpuComponent> gpuComponents=capi.read(productionContainer, gpuSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<Job> jobs=capi.read(jobContainer, jobsSelector, MzsConstants.RequestTimeout.INFINITE, null);

				factoryReference.setCpuComponents(cpuComponents);
				factoryReference.setMainboardComponents(mainboardComponents);
				factoryReference.setRamComponents(ramComponents);
				factoryReference.setGpuComponents(gpuComponents);
				factoryReference.setJobs(jobs);

			}catch(Exception ex){
				System.out.println("Can not interact with factory "+factoryReference.getPort()+". Removing Factory");
				factories.remove(factoryReference.getPort());
			}
			try{
				compareNeededAndStoreComponents();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

	private void compareNeededAndStoreComponents() throws MzsCoreException{

		for(FactoryReference factory:factories.values()){

			int ramsNeeded=factory.getNumberOfRamsNeeded()-factory.getNumberOfRams();
			int mainboardsNeeded=factory.getNumberOfMainboardsNeeded()-factory.getNumberOfMainboards();
			int gpusNeeded=factory.getNumberOfGpusNeeded()-factory.getNumberOfGpus();
			int singleCore16sNeeded=factory.getNumberOfSingleCore16CPUsNeeded()-factory.getNumberOfSingleCore16CPU();
			int dualCore02sNeeded=factory.getNumberOfDualCore02sNeeded()-factory.getNumberOfDualCore02CPU();
			int dualCore24sNeeded=factory.getNumberOfDualCore24sNeeded()-factory.getNumberOfDualCore24CPU();
			
			System.out.println("--------------Factory: "+factory.getPort()+"-------");
			System.out.println("SingleCoreNeeded"+singleCore16sNeeded);
			System.out.println("RamsNeeded"+singleCore16sNeeded);
			System.out.println("MainboardsNeeded"+singleCore16sNeeded);
			System.out.println("GpusNeeded"+singleCore16sNeeded);
			

			for(FactoryReference factory2:factories.values()){
				int countAll=0;
				int ramsStored=factory2.getNumberOfRams();
				if(ramsStored>0){
					if(ramsStored>ramsNeeded){
						ramsStored=ramsNeeded;
					}
					countAll+=ramsStored;
				}
				int mainboardsStored=factory2.getNumberOfMainboards();
				if(mainboardsStored>0){
					if(mainboardsStored>mainboardsNeeded){
						mainboardsStored=mainboardsNeeded;
					}
					countAll+=mainboardsStored;
				}
				int gpusStored=factory2.getNumberOfGpus();
				if(gpusStored>0){
					if(gpusStored>gpusNeeded){
						gpusStored=gpusNeeded;
					}
					countAll+=gpusStored;
				}
				int singleCore16sStored=factory2.getNumberOfSingleCore16CPU();
				System.out.println("Stored-cpus: "+singleCore16sStored);
				if(singleCore16sStored>0){
					if(singleCore16sStored>singleCore16sNeeded){
						singleCore16sStored=singleCore16sNeeded;
					}
					countAll+=singleCore16sStored;
				}
				int dualCore02sStored=factory2.getNumberOfDualCore02CPU();
				if(dualCore02sStored>0){
					if(dualCore02sStored>dualCore02sNeeded){
						dualCore02sStored=dualCore02sNeeded;
					}
					countAll+=dualCore02sStored;
				}
				int dualCore24sStored=factory2.getNumberOfDualCore24CPU();
				if(dualCore24sStored>0){
					if(dualCore24sStored>dualCore24sNeeded){
						dualCore24sStored=dualCore24sNeeded;
					}
					countAll+=dualCore02sStored;
				}

				if(countAll>=10){
					System.out.println("Transfering components");
					TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
					try{
						if(ramsStored>0){
							transferComponents(factory.getProductionContainer(), factory2.getProductionContainer(), new RamComponent(null,null,null),ramsStored);
						}if(mainboardsStored>0){
							transferComponents(factory.getProductionContainer(), factory2.getProductionContainer(), new MainboardComponent(null,null,null),mainboardsStored);
						}if(gpusStored>0){
							transferComponents(factory.getProductionContainer(), factory2.getProductionContainer(), new GpuComponent(null,null,null),gpusStored);
						}if(singleCore16sStored>0){
							transferComponents(factory.getProductionContainer(), factory2.getProductionContainer(), new CpuComponent(null,null,null,CpuType.SINGLE_CORE_16),singleCore16sStored);
						}if(dualCore02sStored>0){
							transferComponents(factory.getProductionContainer(), factory2.getProductionContainer(), new CpuComponent(null,null,null,CpuType.DUAL_CORE_2),dualCore02sStored);
						}if(dualCore24sStored>0){
							transferComponents(factory.getProductionContainer(), factory2.getProductionContainer(), new CpuComponent(null,null,null,CpuType.DUAL_CORE_24),dualCore24sStored);
						}
						ramsNeeded-=ramsStored;
						gpusNeeded-=gpusStored;
						mainboardsNeeded-=mainboardsStored;
						singleCore16sNeeded-=singleCore16sStored;
						dualCore02sNeeded-=dualCore02sStored;
						dualCore24sNeeded-=dualCore24sStored;
					}catch(Exception ex){
						capi.rollbackTransaction(tx);
						System.err.println("Error while transfering component. Transaction rolled back");
					}
				}
			}
		}

	}

	private void transferComponents(ContainerReference factoryTo, ContainerReference factoryFrom,
			ProductComponent componentType, int amount) throws MzsCoreException{

		LindaSelector selector=LindaCoordinator.newSelector(componentType,amount);
		ArrayList<ProductComponent> resultEntries = capi.take(factoryFrom, selector, MzsConstants.RequestTimeout.TRY_ONCE, null);
		System.out.print("Transfered: "+resultEntries.size()+" "+componentType.getClass());
		if(componentType instanceof CpuComponent){
			System.out.println(((CpuComponent)componentType).getCpuType().name());
		}
		
		for(ProductComponent comp:resultEntries){
			capi.write(factoryTo, new Entry(comp));
		}


	}

	public void stop(){
		this.running=false;
	}

	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1,
			List<? extends Serializable> entries) {
		for(Serializable logEntry: entries){
			if(logEntry instanceof Entry){
				Object value = ((Entry)logEntry).getValue();
				if(value instanceof Integer){
					changeFactoryStatus((Integer)value);
				}
			}
		}

	}

	/**
	 * If the fabric is in the hash map ==> remove
	 * Else: Get references to needed containers and add it
	 * @param value: Main port of the fabric
	 */
	private void changeFactoryStatus(Integer value) {
		System.out.println("Changing factory status with port "+value);
		if(factories.containsKey(value)){
			factories.remove(value);
		}else{
			try{
				FactoryReference reference=new FactoryReference();
				reference.setPort(value);
				ContainerReference productionContainer=capi.lookupContainer(SbcConstants.PRODUCERCONTAINER, 
						new URI("xvsm://localhost:"+(value+SbcConstants.PRODUCERPORTOFFSET)), 
						MzsConstants.RequestTimeout.TRY_ONCE, null);
				reference.setProductionContainer(productionContainer);
				ContainerReference orderContainer=capi.lookupContainer(SbcConstants.JOBSCONTAINER, 
						new URI("xvsm://localhost:"+(value)), 
						MzsConstants.RequestTimeout.TRY_ONCE, null);
				reference.setJobsContainer(orderContainer);
				factories.put(value, reference);
			}catch(Exception ex){
				System.err.println("Fabric with port "+value+" could not be added!");
				ex.printStackTrace();
			}
		}


	}

}
