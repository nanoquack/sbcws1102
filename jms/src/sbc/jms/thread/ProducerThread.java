package sbc.jms.thread;

import java.util.List;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.ComponentEnum;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.ProductionOrder;
import sbc.dto.RamComponent;
import sbc.jms.JmsConstants;
import sbc.jms.JmsLogging;

public class ProducerThread implements Runnable {

	private List<ProductionOrder> productionList;
	private int errorRate;
	private String workername;
	private int productSequencer=1;


	public ProducerThread(String workername, List<ProductionOrder> productionList, int errorRate){
		this.workername=workername;
		this.productionList=productionList;
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
			Destination destination = session.createQueue("SbcProducer"+JmsConstants.factoryId);
			// Create a MessageProducer from the Session to the Topic
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			for(ProductionOrder order:productionList){
				int amount=order.getAmount();
				while(amount>0){
					Random rand=new Random();
					int randomTime=rand.nextInt(3)+1;
					Thread.sleep(randomTime*1000);
					double randomFaulty=rand.nextDouble();
					boolean faulty=randomFaulty<=(errorRate/100.0);
					ObjectMessage message=null;
					ProductComponent product=null;
					// Create a messages
					switch(order.getComponent()){
						case CPU:
							product = new CpuComponent(productSequencer++,workername,faulty);
							break;
						case RAM:
							product = new RamComponent(productSequencer++,workername,faulty);
							break;
						case MAINBOARD:
							product = new MainboardComponent(productSequencer++,workername,faulty);
							break;
						case GPU:
							product = new GpuComponent(productSequencer++,workername,faulty);
							break;
						default:
							throw new RuntimeException();
					}

					// Tell the producer to send the message
					message=session.createObjectMessage(product);
					producer.send(message);
					JmsLogging.getInstance().producedComponent(product);
//					System.out.println("Sending product of type "+message.getObject().getClass().toString());
					amount--;
				}
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
