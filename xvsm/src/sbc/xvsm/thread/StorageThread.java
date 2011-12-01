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
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsTimeoutException;


import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.dto.ComponentEnum;
import sbc.dto.ProductComponent;
import sbc.xvsm.storage.Storage;

public class StorageThread implements Runnable {

	private boolean running=true;
	private Storage storage=new Storage();
	private INotifyGui notifyGui;
	private Capi capi;
	private MzsCore core;
	private ContainerReference container;
	
	public StorageThread(INotifyGui notifyGui){
		this.notifyGui=notifyGui;
	}
	
	public void run() {
		try {

			core = DefaultMzsCore.newInstance(0);
			capi = new Capi(core);
			this.container=capi.lookupContainer(SbcConstants.CONTAINER, new URI("xvsm://localhost:12345"), 1000l, null);

			while(running){
				// Wait for entries
				FifoSelector selector=FifoCoordinator.newSelector();
				List<Selector> selectors=new ArrayList<Selector>();
				selectors.add(selector);
				try{
				ArrayList<ProductComponent> resultEntries = capi.take(container, selectors, 1000, null);
				if(resultEntries.size()!=0){
					for(ProductComponent component:resultEntries){
						System.out.println("Worker "+component.getWorker() + 
								" produced "+ component.getClass().toString()+" with Id "
								+component.getId()+(component.isFaulty()?" (faulty!)":""));
						storage.storeItem(component.getClass().getName(), component);
						
						ArrayList<ProductComponent> components=storage.getPcItemsIfAvailable();
						if(components!=null){
//							forwardPcParts(components);	//TODO
						}
//						notifyGui.updateStorage(storage.getStorageState());
						//TODO: notify auskommentieren
					}
				}
				}catch(MzsTimeoutException ex){
					//Hier ist nichts zu machen. Generell ist es imo unnoetig, dass diese
					//Exception ueberhaupt vom Framework geworfen wird.
				}
			}
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}
	
//	private void forwardPcParts(ArrayList<ProductComponent> components) throws JMSException{
//		// Create a ConnectionFactory
//		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
//
//		// Create a Connection
//		Connection connection = connectionFactory.createConnection();
//		connection.start();
//
//		connection.setExceptionListener(this);
//
//		// Create a Session
//		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//		// Create the destination
//		Destination destination = session.createQueue("SbcConstruction");
//		// Create a MessageProducer from the Session to the Topic
//		MessageProducer producer = session.createProducer(destination);
//		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//		
//		// Create a messages
//		ObjectMessage message=session.createObjectMessage(components);
//		// Tell the producer to send the message
//		producer.send(message);
//		System.out.println("PC items sent to construction");
//		producer.close();
//		session.close();
//		connection.close();
//	}
//

	
	public synchronized void stop(){
		running=false;
	}


}
