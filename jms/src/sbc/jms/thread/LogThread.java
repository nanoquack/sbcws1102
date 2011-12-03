package sbc.jms.thread;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.INotifyGui;
import sbc.jms.JmsLogging;

/**
 * LogThread listens on the logger topic and
 * 
 * @author rudolf
 * 
 */
public class LogThread implements Runnable, MessageListener, ExceptionListener {
	private INotifyGui notify;
//	private boolean running;

	public LogThread(INotifyGui notify) {
		this.notify = notify;
//		this.running = true;
	}
	
//	@Override
//	protected void finalize() throws Throwable{
//		
//		super.finalize();
//	}

	@Override
	public void onMessage(Message msg) {
		String logString;
		try {
			if(msg instanceof TextMessage){
				logString = ((TextMessage)msg).getText();
				notify.addLogMessage(logString);
			}
		} catch (JMSException e) {
			notify.addLogMessage(e.getStackTrace().toString());
		}
	}

	@Override
	public void run() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://localhost:61616");
			Connection connection = connectionFactory.createConnection();
			connection.setExceptionListener(this);
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createTopic("SbcLogging");
			MessageConsumer consumer = session.createConsumer(destination);
			consumer.setMessageListener(this);
			connection.start();
			JmsLogging.getInstance().log("Jms LogThread started!");
		} catch (JMSException e) {
			notify.addLogMessage("LogThread: Could not initialize JMS, cause: "
					+ e.getStackTrace().toString());
		}
	}
	
//	public synchronized void stop(){
//		running=false;
//	}

	@Override
	public synchronized void onException(JMSException e) {
		notify.addLogMessage("LogThread: Problem with JMS, cause: "
				+ e.getStackTrace().toString());
	}

}
