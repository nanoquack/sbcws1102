package sbc.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import sbc.dto.StorageState;

public class JmsLogging {
	private static JmsLogging logging;
	private Connection connection;
	private Session session;
	private Destination destination;
	private MessageProducer producer;

	public synchronized static JmsLogging getInstance() {
		if (logging == null) {
			logging = new JmsLogging();
		}
		return logging;
	}

	private JmsLogging() {
		initJms();
	}

	@Override
	protected void finalize() throws Throwable {
		producer.close();
		session.close();
		connection.close();
		super.finalize();
	}

	private void initJms() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://localhost:61616");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createTopic("SbcLogging"
					+ JmsConstants.factoryId);
			producer = session.createProducer(destination);
			connection.start();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void log(String msg) {
		try {
			TextMessage jmsMsg = session.createTextMessage(msg);
			producer.send(jmsMsg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void producedComponent(ProductComponent component) {
		sendStorageState(component, 1);
	}

	public void consumedComponent(ProductComponent component) {
		sendStorageState(component, -1);
	}

	private void sendStorageState(ProductComponent component, int count) {
		try {
			ObjectMessage jmsMsg = session.createObjectMessage();
			jmsMsg.setObject(component);
			jmsMsg.setIntProperty(JmsConstants.PROPERTY_COMPONENT_COUNT, count);
			producer.send(jmsMsg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
