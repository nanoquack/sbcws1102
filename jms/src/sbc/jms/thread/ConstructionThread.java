package sbc.jms.thread;

import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.Computer;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;

public class ConstructionThread implements Runnable, ExceptionListener {

	boolean running=true;

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
			Destination destination = session.createQueue("SbcConstruction");

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			while(running){
				// Wait for a message
				Message m = consumer.receive(1000);
				if(m!=null){
					if (m instanceof ObjectMessage) {
						ObjectMessage message = (ObjectMessage) m;
						List<ProductComponent> components=(List<ProductComponent>)message.getObject();
						Computer computer=new Computer();
						while(components.size()!=0){
							ProductComponent comp=components.remove(0);
							if(comp instanceof CpuComponent){
								computer.setCpu((CpuComponent)comp);
							}if(comp instanceof MainboardComponent){
								computer.setMainboard((MainboardComponent)comp);
							}if(comp instanceof GpuComponent){
								computer.setGpu((GpuComponent)comp);
							}if(comp instanceof RamComponent){
								computer.addRam((RamComponent)comp);
							}
						}
						System.out.println("Computer constructed");
					}else {
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
		stop();
	}
	
	public synchronized void stop(){
		running=false;
	}

}
