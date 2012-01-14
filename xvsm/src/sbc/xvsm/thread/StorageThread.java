package sbc.xvsm.thread;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.QueryCoordinator.QuerySelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.Entry;


import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.dto.ComponentEnum;
import sbc.dto.ProductComponent;
import sbc.xvsm.storage.Storage;

@Deprecated
public class StorageThread implements Runnable {

	private boolean running=false;    //Running=false, storage thread is deactivated
	private Storage storage=new Storage();
	private INotifyGui notifyGui;
	private Capi capi;
	private MzsCore core;
	private ContainerReference producerContainer;
	private ContainerReference storageContainer;

	public StorageThread(INotifyGui notifyGui){
		this.notifyGui = notifyGui;
	}
	
	public void run() {
		try {

			core = DefaultMzsCore.newInstance(SbcConstants.MAINPORT+SbcConstants.STORAGEPORTOFFSET);
			capi = new Capi(core);
			this.producerContainer=capi.lookupContainer(SbcConstants.PRODUCERCONTAINER, new URI(SbcConstants.ProducerUrl), 1000l, null);
			this.storageContainer=capi.createContainer(
					SbcConstants.STORAGECONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new FifoCoordinator());

			while(running){
				// Wait for entries
				FifoSelector selector=FifoCoordinator.newSelector();
				List<Selector> selectors=new ArrayList<Selector>();
				selectors.add(selector);
				try{
					//TODO: MaxEntries auf hoeheren Wert setzen
					ArrayList<ProductComponent> resultEntries = capi.take(producerContainer, selectors, 1, null);
					for(ProductComponent component:resultEntries){
						System.out.println("Worker "+component.getWorker() + 
								" produced "+ component.getClass().toString()+" with Id "
								+component.getId()+(component.isFaulty()?" (faulty!)":""));
						storage.storeItem(component.getClass().getName(), component);
						notifyGui.updateStorage(storage.getStorageState());
						ArrayList<ProductComponent> components=storage.getPcItemsIfAvailable();
						if(components!=null){
							Entry e=new Entry(components);
						    capi.write(storageContainer, e);
						    notifyGui.addLogMessage("Computer components sent to construction");
						}
					}
				}catch(MzsTimeoutException ex){
					//Hier ist nichts zu machen. Timeout.Infinite nicht moeglich, sonst
					//stoppt dieser Thread nicht mehr.
				}
			}
			
			core.shutdown(true);
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	public synchronized void stop(){
		running=false;
	}


}
