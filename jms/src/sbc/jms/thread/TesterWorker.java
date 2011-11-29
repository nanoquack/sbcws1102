package sbc.jms.thread;

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
import sbc.dto.RamComponent;

public class TesterWorker  implements Runnable, ExceptionListener {

	boolean running=true;
	
	public static void main(String[] args){
		TesterWorker tester = new TesterWorker();
		Thread t = new Thread(tester);
		t.start();
		System.out.println("Test worker started");
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
			Destination destination = session.createQueue("SbcTesting");

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			while(running){
				// Wait for a message
				Message m = consumer.receive(1000);
				if(m!=null){
					if (m instanceof ObjectMessage) {
						ObjectMessage message = (ObjectMessage) m;
						Computer computer=(Computer)message.getObject();
						computer.setQualityCheckPassed(true);
						if(computer.getCpu()==null || computer.getCpu().isFaulty()==true){
							computer.setQualityCheckPassed(false);
						}if(computer.getMainboard()==null || computer.getMainboard().isFaulty()==true){
							computer.setQualityCheckPassed(false);
						}if(computer.getGpu()!=null && computer.getGpu().isFaulty()==true){
							computer.setQualityCheckPassed(false);
						}if(computer.getRam()==null){
							computer.setQualityCheckPassed(false);
						}for(RamComponent ram:computer.getRam()){
							if(ram.isFaulty()){computer.setQualityCheckPassed(false);}
						}
						System.out.println(computer.getQualityCheckPassed()?"Computer ok":"Computer faulty");
						forwardPc(computer);
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
		Destination destination = session.createQueue("SbcLogistic");
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		// Create a messages
		ObjectMessage message=session.createObjectMessage(computer);
		// Tell the producer to send the message
		producer.send(message);
		System.out.println("PC sent to logistic");
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
