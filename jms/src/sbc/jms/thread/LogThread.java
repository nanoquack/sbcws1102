package sbc.jms.thread;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.INotifyGui;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import sbc.dto.StorageState;
import sbc.jms.JmsConstants;
import sbc.jms.JmsLogging;

/**
 * LogThread listens on the logger topic and
 * 
 * @author rudolf
 * 
 */
public class LogThread implements Runnable, MessageListener, ExceptionListener {
	private INotifyGui notify;
	private StorageState storageState;

	public LogThread(INotifyGui notify) {
		this.notify = notify;
		this.storageState = new StorageState();
	}

	@Override
	public void onMessage(Message msg) {
		String logString;
		try {
			if(msg instanceof TextMessage){
				logString = ((TextMessage)msg).getText();
				notify.addLogMessage(logString);
			}
			else if(msg instanceof ObjectMessage){
				ObjectMessage objMsg = (ObjectMessage)msg;
				ProductComponent component = (ProductComponent)objMsg.getObject();
				int productCount = objMsg.getIntProperty(JmsConstants.PROPERTY_COMPONENT_COUNT);
				
				synchronized(storageState){
					if(component instanceof CpuComponent){
						storageState.setCpu(storageState.getCpu()+productCount);
					}
					if(component instanceof MainboardComponent){
						storageState.setMainboard(storageState.getMainboard()+productCount);
					}
					if(component instanceof RamComponent){
						storageState.setRam(storageState.getRam()+productCount);
					}
					if(component instanceof GpuComponent){
						storageState.setGpu(storageState.getGpu()+productCount);
					}
					notify.updateStorage(storageState);
				}
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
			Destination destination = session.createTopic("SbcLogging"+JmsConstants.factoryId);
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
