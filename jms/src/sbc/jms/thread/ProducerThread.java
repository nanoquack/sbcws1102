package sbc.jms.thread;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.ComponentEnum;
import sbc.dto.ProductComponent;

public class ProducerThread implements Runnable {

	private int amount;
	private int errorRate;
	private String workername;
	private int productSequencer=1;


	public ProducerThread(String workername, int amount, int errorRate){
		this.workername=workername;
		this.amount=amount;
		this.errorRate=errorRate;
	}

	@Override
	public void run() {
		try{
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();
			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// Create the destination
			Destination destination = session.createQueue("SBC");
			// Create a MessageProducer from the Session to the Topic
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			while(amount>0){
				//TODO: 1-3 seconds production time
				//TODO: add error rate by setting faulty
				//TODO: produce more than just 1 CPU :-)
				// Create a messages
				ObjectMessage message=session.createObjectMessage(new ProductComponent(productSequencer++,workername,ComponentEnum.CPU));
				// Tell the producer to send the message
				producer.send(message);
				amount--;
			}

			// Clean up
			session.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("ProducerThread - error while interaction with ActiveMQ.");
			e.printStackTrace();
		}

	}

}
