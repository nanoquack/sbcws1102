package sbc.jms.thread;

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

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.Computer;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import sbc.jms.JmsConstants;
import sbc.jms.JmsLogging;

public class ConstructionWorker implements Runnable, ExceptionListener {

	private boolean running=true;

	public static void main(String[] args){
		if(args.length!=1){
			System.err.println("Factorya id has to be specified");
			System.exit(1);
		}
		JmsConstants.factoryId=args[0];
		ConstructionWorker constructor = new ConstructionWorker();
		Thread t = new Thread(constructor);
		t.start();
		JmsLogging.getInstance().log("Construction worker started");
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
			Destination destination = session.createQueue("SbcConstruction"+JmsConstants.factoryId);

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
						forwardPc(computer);
						JmsLogging.getInstance().log("Computer constructed");
					}else {
						JmsLogging.getInstance().log("Dropped message "+m.getJMSMessageID());
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

	private void forwardPc(Computer computer) throws JMSException{

		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Create the destination
		Destination destination = session.createQueue("SbcTesting"+JmsConstants.factoryId);
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		// Create a messages
		ObjectMessage message=session.createObjectMessage(computer);
		// Tell the producer to send the message
		producer.send(message);
		JmsLogging.getInstance().log("PC sent to testing");
		producer.close();
		session.close();
		connection.close();
	}

public synchronized void onException(JMSException ex) {
	JmsLogging.getInstance().log("JMS Exception occured.  Shutting down client.");
	stop();
}

public synchronized void stop(){
	running=false;
}

}
