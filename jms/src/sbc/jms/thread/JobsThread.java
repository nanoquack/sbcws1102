package sbc.jms.thread;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.Computer;
import sbc.jms.JmsConstants;
import sbc.jms.JmsLogging;
import sbc.job.Configuration;
import sbc.job.Job;

public class JobsThread implements Runnable, ExceptionListener {

	private ArrayList<Job> jobs=new ArrayList<Job>();
	private boolean running = true;

	public synchronized void addJob(Job job){
		jobs.add(job);
	}
	
	public List<Job> getJobs(){
		return jobs;
	}

	@Override
	public void run() {
		try{
//			configureJobsListener();	//Wird direkt uebergeben, nicht benoetigt (schade :-) )

			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			connection.setExceptionListener(this);

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createQueue("SbcDelivery"+JmsConstants.factoryId);

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);
			
			while(running){
				Message m = consumer.receive(10000);
				if(m!=null){
					if (m instanceof ObjectMessage) {
						ObjectMessage message = (ObjectMessage) m;
						Computer computer=(Computer) message.getObject();
						if(jobs.size()==0){continue;}
						int quantity=jobs.get(0).getQuantity();
						if(--quantity==0){
							JmsLogging.getInstance().log("Job "+jobs.get(0).getUuid()+" completed!");
							jobs.remove(0);
						}else{
							JmsLogging.getInstance().log("Quantity job "+jobs.get(0).getUuid()+" "+quantity);
							jobs.get(0).setQuantity(quantity);
						}
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	//TODO: zuerst muss storage umgebaut werden, um richtige PCs zu erzeugen
	private boolean fitsJob(Computer comp, Job job){
		Configuration conf=job.getConfiguration();
		if(!(comp.getCpu().equals(conf.getCpuType()))){
			return false;
		}if(!(comp.getRam().size()==conf.getRamModuleCount())){
			return false;
		}if(!((comp.getGpu()==null)==conf.isGraphicsCard())){
			return false;
		}
		return true;
	}

	//Wird doch nicht benoetigt, da die Jobs direkt an den Thread uebergeben werden
	//Schade drum, die Implementierung ueber den Listener sah gut aus
	private void configureJobsListener() throws Exception {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
		"tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue("SbcJobs");
		MessageConsumer consumer = session.createConsumer(destination);

		MessageListener listener = new MessageListener() {
			public void onMessage(Message msg) {
				try{
					ObjectMessage message = (ObjectMessage) msg;
					Job job=(Job)message.getObject();
					addJob(job);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		};
		consumer.setMessageListener(listener);

		connection.start();
	}

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
		stop();
	}

	public void stop(){
		running=false;
	}




}
