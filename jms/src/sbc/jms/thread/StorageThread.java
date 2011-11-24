package sbc.jms.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.INotifyGui;
import sbc.dto.ComponentEnum;
import sbc.dto.ProductComponent;
import sbc.jms.storage.Storage;

public class StorageThread implements Runnable, ExceptionListener {

	private boolean running=true;
	private Storage storage=new Storage();
	private INotifyGui notifyGui;
	
	public StorageThread(INotifyGui notifyGui){
		this.notifyGui=notifyGui;
	}
	
	public void run() {
		try {

			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			connection.setExceptionListener(this);

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createQueue("SbcProducer");

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			while(running){
				// Wait for a message
				Message m = consumer.receive(1000);
				if(m!=null){
					if (m instanceof ObjectMessage) {
						ObjectMessage message = (ObjectMessage) m;
						ProductComponent component = (ProductComponent) message.getObject();
						System.out.println("Worker "+component.getWorker() + 
								" produced "+ component.getClass().toString()+" with Id "
								+component.getId()+(component.isFaulty()?" (faulty!)":""));
						storage.storeItem(component.getClass().getName(), component);
						
						ArrayList<ProductComponent> components=storage.getPcItemsIfAvailable();
						if(components!=null){
							forwardPcParts(components);
						}
						notifyGui.updateStorage(storage.getStorageState());
					} else {
						System.out.println("Dropped message "+m.getJMSMessageID());
					}
				}
			}

			consumer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}
	
	private void forwardPcParts(ArrayList<ProductComponent> components) throws JMSException{
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Create the destination
		Destination destination = session.createQueue("SbcConstruction");
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		
		// Create a messages
		ObjectMessage message=session.createObjectMessage(components);
		// Tell the producer to send the message
		producer.send(message);
		System.out.println("PC items sent to construction");
		producer.close();
		session.close();
		connection.close();
	}

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
		stop();
	}
	
	public synchronized void stop(){
		running=false;
	}


}
