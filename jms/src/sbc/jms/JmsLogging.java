package sbc.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

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
			destination = session.createTopic("SbcLogging");
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
}
