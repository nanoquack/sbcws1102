package sbc.loadbalancing;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
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

import sbc.SbcConstants;
import sbc.dto.ComponentEnum;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.ProductionOrder;
import sbc.dto.RamComponent;

public class LoadBalancer implements Runnable, NotificationListener {

	//List of all demanded components
	//	private List<ProductionOrder> orderList;

	private ContainerReference registrationContainer;
	private Capi capi;
	private MzsCore core;

	//TODO: Obtain reference of other fabrics
	private HashMap<Integer,FactoryReference> factories;

	private boolean running=true;


	/**
	 * Check regularly if components should be transfered
	 * Components are transfered, if a certain limit has been exceeded
	 * All needed information is extracted from the containers and
	 * the current list of orders.
	 */
	@Override
	public void run() {
		try{
			core = DefaultMzsCore.newInstance(SbcConstants.LoadBalancerPort);
			capi = new Capi(core);

			registrationContainer = capi.createContainer(
					SbcConstants.LOADBALANCERCONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new QueryCoordinator());
			NotificationManager notifManager = new NotificationManager(core);
			notifManager.createNotification(registrationContainer, this, Operation.WRITE);

			while(running){
				Thread.sleep(1000);
				checkOverallProductionStatus();
			}
		}catch (Exception e) {
			System.err.println("Error in LoadBalancer-Thread occured");
			e.printStackTrace();
		}
	}

	private void checkOverallProductionStatus() {
		
		LindaSelector cpuSelector=LindaCoordinator.newSelector(new CpuComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		LindaSelector mainboardSelector=LindaCoordinator.newSelector(new MainboardComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		LindaSelector ramSelector=LindaCoordinator.newSelector(new RamComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		LindaSelector gpuSelector=LindaCoordinator.newSelector(new GpuComponent(null,null,null),MzsConstants.Selecting.COUNT_MAX);
		
		for(FactoryReference factoryReference:factories.values()){
			ContainerReference productionContainer=factoryReference.getProductionContainer();
			ContainerReference orderContainer=factoryReference.getOrdersContainer();
			try{
				ArrayList<CpuComponent> cpuComponents=capi.read(productionContainer, cpuSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<MainboardComponent> mainboardComponents=capi.read(productionContainer, mainboardSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<RamComponent> ramComponents=capi.read(productionContainer, ramSelector, MzsConstants.RequestTimeout.INFINITE, null);
				ArrayList<GpuComponent> gpuComponents=capi.read(productionContainer, gpuSelector, MzsConstants.RequestTimeout.INFINITE, null);
				//TODO: Get all current orders
				
				factoryReference.setCpuComponents(cpuComponents);
				factoryReference.setMainboardComponents(mainboardComponents);
				factoryReference.setRamComponents(ramComponents);
				factoryReference.setGpuComponents(gpuComponents);
				
			}catch(Exception ex){
				System.out.println("Can not interact with factory "+factoryReference.getPort()+". Removing Factory");
				factories.remove(factoryReference.getPort());
			}
			
		}
		
		for(FactoryReference factoryReference:factories.values()){
			
			//TODO: If(factory.order-factory.productioncontainer > 10)
			for(FactoryReference factoryReference2:factories.values()){
			//TODO: Check if there is a factory that can supply
				//Check by factory itself no problem (can't have products that are needed)
				//If found:
//				transferComponents(factoryReference, factoryReference2, 
//						ProductComponentType (zB.: new RamComponent(null,null,null))),
//						amount);
				//TODO: Achtung: Beim CPU-Typ genau schauen, ob dieser stimmt
			}
		}
	}
	
	private void transferComponents(ContainerReference factoryTo, ContainerReference factoryFrom,
			ProductComponent componentType, int amount) throws MzsCoreException{
		TransactionReference tx = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, null);
		try{
			LindaSelector selector=LindaCoordinator.newSelector(componentType,amount);
			ArrayList<ProductComponent> resultEntries = capi.take(factoryFrom, selector, MzsConstants.RequestTimeout.TRY_ONCE, null);

			for(ProductComponent comp:resultEntries){
				capi.write(factoryTo, new Entry(comp));
			}
			
		}catch(Exception ex){
			capi.rollbackTransaction(tx);
			System.err.println("Error while transfering component. Transaction rolled back");
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
		if(factories.containsKey(value)){
			factories.remove(value);
		}else{
			try{
				FactoryReference reference=new FactoryReference();
				reference.setPort(value);
				ContainerReference productionContainer=capi.lookupContainer(SbcConstants.PRODUCERCONTAINER, 
						new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)), 
						MzsConstants.RequestTimeout.INFINITE, null);
				reference.setProductionContainer(productionContainer);
				//TODO: Add orderContainer
			}catch(Exception ex){
				System.err.println("Fabric with port "+value+" could not be added!");
				ex.printStackTrace();
			}
		}


	}

}
