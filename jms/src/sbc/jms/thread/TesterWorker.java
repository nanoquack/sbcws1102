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
import sbc.jms.JmsLogging;

public class TesterWorker  implements Runnable, ExceptionListener {

	private String queueName;
	private boolean running=true;

	/**
	 * Main method of the testers.
	 * If args[0] contains "1" ==> Start Tester that controlls pc parts completeness
	 * If args[0] contains "2" ==> Start Tester that checks faultiness of pc parts
	 */
	public static void main(String[] args){
		TesterWorker tester=null;
		if(args[0].equals("1")){
			tester = new TesterWorker("SbcTesting");
		}
		if(args[0].equals("2")){
			tester = new TesterWorker("SbcTesting2");
		}
		JmsLogging.getInstance().log(args[0]);
		if((!args[0].equals("1")) && (!args[0].equals("2"))){
			throw new RuntimeException("Usage: TesterWorker <1>/<2>");
		}
		Thread t = new Thread(tester);
		t.start();
		JmsLogging.getInstance().log("Test worker started");
	}

	public TesterWorker(String queueName){
		this.queueName=queueName;
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
			Destination destination = session.createQueue(queueName);

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			while(running){
				// Wait for a message
				Message m = consumer.receive(1000);
				if(m!=null){
					if (m instanceof ObjectMessage) {

						ObjectMessage message = (ObjectMessage) m;
						Computer computer=(Computer)message.getObject();
						
						//If completness check has not yet been made, do it
						//Else: check if parts are faulty
						if(computer.getIsComplete()==null){
							computer.setIsComplete(true);
							if(computer.getCpu()==null){
								computer.setIsComplete(false);
							}if(computer.getRam()==null){
								computer.setIsComplete(false);
							}if(computer.getRam().size()==0){
								computer.setIsComplete(false);
							}
							if(computer.getMainboard()==null){
								computer.setIsComplete(false);
							}
							forwardPcToSecondTester(computer);
						}else if(computer.getIsComplete()==false){
							computer.setQualityCheckPassed(false);
						}else{

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
							JmsLogging.getInstance().log(computer.getQualityCheckPassed()?"Computer ok":"Computer faulty");
							forwardPcToLogistic(computer);
						}
					} else {
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

	private void forwardPcToSecondTester(Computer computer) throws JMSException{

		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Create the destination
		Destination destination = session.createQueue("SbcTesting2");
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		// Create a messages
		ObjectMessage message=session.createObjectMessage(computer);
		// Tell the producer to send the message
		producer.send(message);
		JmsLogging.getInstance().log("PC sent to second tester");
		producer.close();
		session.close();
		connection.close();
	}

	
	private void forwardPcToLogistic(Computer computer) throws JMSException{

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
		JmsLogging.getInstance().log("PC sent to logistic");
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
