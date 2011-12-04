package sbc.jms.thread;

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
import sbc.jms.JmsLogging;

public class LogisticWorker  implements Runnable, ExceptionListener {

	boolean running=true;
	
	public static void main(String[] args){
		LogisticWorker logistics = new LogisticWorker();
		Thread t = new Thread(logistics);
		t.start();
		JmsLogging.getInstance().log("Logistic worker started");
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
			Destination destination = session.createQueue("SbcLogistic");

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			while(running){
				// Wait for a message
				Message m = consumer.receive(1000);
				if(m!=null){
					if (m instanceof ObjectMessage) {
						ObjectMessage message = (ObjectMessage) m;
						Computer computer=(Computer) message.getObject();
						if(computer.getQualityCheckPassed()){
							String logMsg = "Computer delieverd for sale: \n" + computer.toString();
							JmsLogging.getInstance().log(logMsg);
						}else{
							String logMsg = "Computer stored in recycling storage: \n" + computer.toString();
							JmsLogging.getInstance().log(logMsg);
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

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
		stop();
	}
	
	public synchronized void stop(){
		running=false;
	}

}
