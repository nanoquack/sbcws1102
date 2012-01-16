package sbc.jms.thread;

import java.util.ArrayList;
import java.util.LinkedList;

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

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.INotifyGui;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import sbc.dto.CpuComponent.CpuType;
import sbc.jms.JmsConstants;
import sbc.jms.JmsLogging;
import sbc.jms.storage.Storage;

public class StorageThread implements Runnable, ExceptionListener {

	private boolean running = true;
	private Storage storage = new Storage();
	private INotifyGui notifyGui;

	public StorageThread(INotifyGui notifyGui) {
		this.notifyGui = notifyGui;
	}

	public void run() {
		try {

			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://localhost:61616");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			connection.setExceptionListener(this);

			// Create a Session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createQueue("SbcProducer"+JmsConstants.factoryId);

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			while (running) {
				// Wait for a message
				Message m = consumer.receive(1000);
				if (m != null) {
					if (m instanceof ObjectMessage) {
						ObjectMessage message = (ObjectMessage) m;
						ProductComponent component = (ProductComponent) message
								.getObject();
						// JmsLogging.getInstance().log("Worker "+component.getWorker()
						// +
						// " produced "+
						// component.getClass().toString()+" with Id "
						// +component.getId()+(component.isFaulty()?" (faulty!)":""));
						// storage.storeItem(component.getClass().getName(),
						// component);
						JmsLogging.getInstance().log(
								"Component Produced " + component.toString());
						storage.storeItem(component.getClass().getName(),
								component);

						ArrayList<ProductComponent> components = storage
								.getPcItemsIfAvailable();
						if (components != null) {
							forwardPcParts(components);
						}
						notifyGui.updateStorage(storage.getStorageState());
					} else {
						JmsLogging.getInstance().log(
								"Dropped message " + m.getJMSMessageID());
					}
				}
			}

			consumer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
			JmsLogging.getInstance().log("Caught: " + e);
			e.printStackTrace();
		}
	}

	private void forwardPcParts(ArrayList<ProductComponent> components)
			throws JMSException {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		// Create the destination
		Destination destination = session.createQueue("SbcConstruction"+JmsConstants.factoryId);
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		// Create a messages
		ObjectMessage message = session.createObjectMessage(components);
		// Tell the producer to send the message
		producer.send(message);
		JmsLogging.getInstance().log("PC items sent to construction");
		producer.close();
		session.close();
		connection.close();
	}

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
		stop();
	}

	public synchronized void stop() {
		running = false;
	}
	
	public synchronized int getNumberOfRams(){
		return storage.getNumberOfRams();
	}
	
	public synchronized int getNumberOfMainboards(){
		return storage.getNumberOfMainboards();
	}
	
	public synchronized int getNumberOfGpus(){
		return storage.getNumberOfGpus();
	}
	
	public synchronized int getNumberOfSingleCore16CPU(){
		return storage.getNumberOfSingleCore16CPU();
	}
	public synchronized int getNumberOfDualCore02CPU(){
		return storage.getNumberOfDualCore02CPU();
	}
	
	public synchronized int getNumberOfDualCore24CPU(){
		return storage.getNumberOfDualCore24CPU();
	}
	
	public ProductComponent removeComponent(ProductComponent comp){
		System.out.println("Removing Component of typ "+comp.getClass());
		ProductComponent c=storage.removeComponent(comp);
		notifyGui.updateStorage(storage.getStorageState());
		return c;
	}

}
