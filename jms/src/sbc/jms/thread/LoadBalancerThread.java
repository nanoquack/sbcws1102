package sbc.jms.thread;

import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import sbc.dto.CpuComponent.CpuType;
import sbc.jms.JmsConstants;
import sbc.jms.JmsLogging;
import sbc.jms.storage.NeededComponents;
import sbc.job.Job;

public class LoadBalancerThread implements Runnable, ExceptionListener {

	private JobsThread jobThread;
	private StorageThread storageThread;
	private boolean running=true;

	public LoadBalancerThread(JobsThread jobThread, StorageThread storageThread){
		this.jobThread=jobThread;
		this.storageThread=storageThread;
	}


	@Override
	public void run() {

		try{
			configureJobsListener();

			List<Job> jobs=jobThread.getJobs();
			while(running){
				Thread.sleep(10000);
				int ramsNeeded=getNumberOfRamsNeeded(jobs)-storageThread.getNumberOfRams();
				int mainboardsNeeded=getNumberOfMainboardsNeeded(jobs)-storageThread.getNumberOfMainboards();
				int gpusNeeded=getNumberOfGpusNeeded(jobs)-storageThread.getNumberOfGpus();
				int singleCore16sNeeded=getNumberOfSingleCore16CPUsNeeded(jobs)-storageThread.getNumberOfSingleCore16CPU();
				int dualCore02sNeeded=getNumberOfDualCore02sNeeded(jobs)-storageThread.getNumberOfDualCore02CPU();
				int dualCore24sNeeded=getNumberOfDualCore24sNeeded(jobs)-storageThread.getNumberOfDualCore24CPU();
				//Calculate current status with methods from xvsm-loadbalancer (siehe unten)
				NeededComponents nc=new NeededComponents(JmsConstants.factoryId,ramsNeeded,mainboardsNeeded,
						gpusNeeded,singleCore16sNeeded,dualCore02sNeeded,dualCore24sNeeded);

				pushStatusToTopic(nc);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

	}

	private void pushStatusToTopic(NeededComponents nc) throws Exception{
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Create the destination
		Destination destination = session.createTopic("SbcComponentsNeeded");
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		// Create a messages
		ObjectMessage message=session.createObjectMessage(nc);
		// Tell the producer to send the message
		producer.send(message);
		producer.close();
		session.close();
		connection.close();
	}

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
		Topic destination = session.createTopic("SbcComponentsNeeded");
		MessageConsumer consumer = session.createConsumer(destination);

		MessageListener listener = new MessageListener() {
			public void onMessage(Message msg) {
				try{
					if(msg!=null){
						if (msg instanceof ObjectMessage) {
							ObjectMessage message = (ObjectMessage) msg;
							NeededComponents nc=(NeededComponents)message.getObject();
							int ramsNeeded=nc.getRamsNeeded();
							int mainboardsNeeded=nc.getMainboardsNeeded();
							int gpusNeeded=nc.getGpusNeeded();
							int singleCore16sNeeded=nc.getSingleCore16sNeeded();
							int dualCore02sNeeded=nc.getDualCore02sNeeded();
							int dualCore24sNeeded=nc.getDualCore24sNeeded();

							System.out.println("------------Factory "+JmsConstants.factoryId+"--------");
							System.out.print("RamsNeeded: "+ramsNeeded);

							int countAll=0;
							int ramsStored=storageThread.getNumberOfRams();
							System.out.println(", RamsStored: "+ramsStored);
							if(ramsStored>0){
								if(ramsStored>ramsNeeded){
									ramsStored=ramsNeeded;
								}
								countAll+=ramsStored;
							}
							int mainboardsStored=storageThread.getNumberOfMainboards();
							if(mainboardsStored>0){
								if(mainboardsStored>mainboardsNeeded){
									mainboardsStored=mainboardsNeeded;
								}
								countAll+=mainboardsStored;
							}
							int gpusStored=storageThread.getNumberOfGpus();
							if(gpusStored>0){
								if(gpusStored>gpusNeeded){
									gpusStored=gpusNeeded;
								}
								countAll+=gpusStored;
							}
							int singleCore16sStored=storageThread.getNumberOfSingleCore16CPU();
							System.out.println("Stored-cpus: "+singleCore16sStored);
							if(singleCore16sStored>0){
								if(singleCore16sStored>singleCore16sNeeded){
									singleCore16sStored=singleCore16sNeeded;
								}
								countAll+=singleCore16sStored;
							}
							int dualCore02sStored=storageThread.getNumberOfDualCore02CPU();
							if(dualCore02sStored>0){
								if(dualCore02sStored>dualCore02sNeeded){
									dualCore02sStored=dualCore02sNeeded;
								}
								countAll+=dualCore02sStored;
							}
							int dualCore24sStored=storageThread.getNumberOfDualCore24CPU();
							if(dualCore24sStored>0){
								if(dualCore24sStored>dualCore24sNeeded){
									dualCore24sStored=dualCore24sNeeded;
								}
								countAll+=dualCore02sStored;
							}
							System.out.println("CountAll: "+countAll);
							if(countAll>=10){
								System.out.println("Transfering components");
								try{
									if(ramsStored>0){
										transferComponents(nc.getFactoryId(), new RamComponent(null,null,null),ramsStored);
									}if(mainboardsStored>0){
										transferComponents(nc.getFactoryId(), new MainboardComponent(null,null,null),mainboardsStored);
									}if(gpusStored>0){
										transferComponents(nc.getFactoryId(), new GpuComponent(null,null,null),gpusStored);
									}if(singleCore16sStored>0){
										transferComponents(nc.getFactoryId(), new CpuComponent(null,null,null,CpuType.SINGLE_CORE_16),singleCore16sStored);
									}if(dualCore02sStored>0){
										transferComponents(nc.getFactoryId(), new CpuComponent(null,null,null,CpuType.DUAL_CORE_2),dualCore02sStored);
									}if(dualCore24sStored>0){
										transferComponents(nc.getFactoryId(), new CpuComponent(null,null,null,CpuType.DUAL_CORE_24),dualCore24sStored);
									}
									ramsNeeded-=ramsStored;
									gpusNeeded-=gpusStored;
									mainboardsNeeded-=mainboardsStored;
									singleCore16sNeeded-=singleCore16sStored;
									dualCore02sNeeded-=dualCore02sStored;
									dualCore24sNeeded-=dualCore24sStored;
								}catch(Exception ex){
									System.err.println("Error while transfering component. Transaction rolled back");
									ex.printStackTrace();
								}
							}
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		};
		consumer.setMessageListener(listener);

		connection.start();
	}

	private void transferComponents(String factoryId, ProductComponent comp, int amount) throws Exception{
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		connection.setExceptionListener(this);

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Create the destination
		Destination destination = session.createQueue("SbcProducer"+factoryId);
		// Create a MessageProducer from the Session to the Topic
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		for(int i=0;i<amount;i++){
			ProductComponent component=storageThread.removeComponent(comp);
			if(component!=null){
				ObjectMessage message=session.createObjectMessage(component);
				producer.send(message);
				addLogsInFabrik(factoryId,component);
			}
		}
		producer.close();
		session.close();
		connection.close();
	}

	private void addLogsInFabrik(String factoryId, ProductComponent component) throws Exception{
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
		"tcp://localhost:61616");
		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic("SbcLogging"+ factoryId);
		MessageProducer producer = session.createProducer(destination);
		connection.start();
		ObjectMessage jmsMsg = session.createObjectMessage();
		jmsMsg.setObject(component);
		jmsMsg.setIntProperty(JmsConstants.PROPERTY_COMPONENT_COUNT, 1);
		producer.send(jmsMsg);
		producer.close();
		session.close();
		connection.close();
	}

	public void stop(){
		running=false;
	}


	@Override
	public void onException(JMSException arg0) {
		arg0.printStackTrace();

	}

	private int getNumberOfMainboardsNeeded(List<Job> jobs){
		int i=0;
		for(Job job:jobs){
			i+=job.getQuantity();
		}
		return i;
	}

	private int getNumberOfGpusNeeded(List<Job> jobs){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().isGraphicsCard()){
				i+=job.getQuantity();
			}
		}
		return i;
	}

	private int getNumberOfRamsNeeded(List<Job> jobs){
		int i=0;
		for(Job job:jobs){
			int rmc=job.getConfiguration().getRamModuleCount();
			int j=rmc*job.getQuantity();
			i=i+j;
		}
		return i;
	}

	private int getNumberOfDualCore02sNeeded(List<Job> jobs){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().getCpuType().equals(CpuType.DUAL_CORE_2)){
				i+=job.getQuantity();
			}
		}
		return i;
	}

	private int getNumberOfDualCore24sNeeded(List<Job> jobs){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().getCpuType().equals(CpuType.DUAL_CORE_24)){
				i+=job.getQuantity();
			}
		}
		return i;
	}

	private int getNumberOfSingleCore16CPUsNeeded(List<Job> jobs){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().getCpuType().equals(CpuType.SINGLE_CORE_16)){
				i+=job.getQuantity();
			}
		}
		return i;
	}
}
