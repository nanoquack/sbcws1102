package sbc.jms.thread;

import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.ComponentEnum;
import sbc.dto.ProductComponent;

public class StorageThread implements Runnable, ExceptionListener {

	boolean running=true;
	
	HashMap<ComponentEnum,Object> storage=new HashMap<ComponentEnum,Object>();
	
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
			Destination destination = session.createQueue("SBC");

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
								" produced "+ component.getComponent().toString()+" with Id "
								+component.getId()+(component.isFaulty()?" (faulty!)":""));
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

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
		//TODO: Shut down
	}
	
	public synchronized void stop(){
		running=false;
	}


}
