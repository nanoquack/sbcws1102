package sbc.jms;

import java.util.List;

import sbc.IBackend;
import sbc.INotifyGui;
import sbc.dto.ProductionOrder;
import sbc.jms.thread.ConstructionWorker;
import sbc.jms.thread.JobsThread;
import sbc.jms.thread.LoadBalancerThread;
import sbc.jms.thread.LogThread;
import sbc.jms.thread.LogisticWorker;
import sbc.jms.thread.ProducerThread;
import sbc.jms.thread.StorageThread;
import sbc.jms.thread.TesterWorker;
import sbc.job.Configuration;
import sbc.job.Job;

public class Backend implements IBackend {

	private StorageThread st;
	private Thread logThread;
	private JobsThread jt;
	private LoadBalancerThread lb;
	private int workerSequencer = 1;

	/**
	 * Start Construction, Test and Logistic part of the factory Additionally
	 * initialize intermediate Storage for storing computer parts
	 */
	public void initializeFactory(INotifyGui notifyGui) {
		st = new StorageThread(notifyGui);
		Thread storageThread = new Thread(st);
		storageThread.start();
		
		logThread = new Thread(new LogThread(notifyGui));
		// set log thread as daemon thread, so process will be terminated if
		// backend thread is stopped
		logThread.setDaemon(true);
		logThread.start();
		
		jt=new JobsThread();
		Thread jobThread=new Thread(jt);
		jobThread.start();
		
		lb=new LoadBalancerThread(jt,st);
		Thread lbThread=new Thread(lb);
		lbThread.start();
	}

	/**
	 * Creates new Threads with the specified parameters
	 */
	@Override
	public void createProducer(List<ProductionOrder> productionList,
			int errorRate) {
		String workername = "worker" + workerSequencer++;
		ProducerThread pt = new ProducerThread(workername, productionList,
				errorRate);
		Thread producerThread = new Thread(pt);
		producerThread.start();
	}

	public void shutDownFactory() {
		st.stop();
		jt.stop();
	}

	@Override
	public void startSystem(INotifyGui notifyGui, String factoryId) {
		JmsConstants.factoryId=factoryId;
		initializeFactory(notifyGui);
	}

	@Override
	public void shutdownSystem() {
		shutDownFactory();

	}
	
	@Override
	public void createJob(Job job){
		jt.addJob(job);
	}

}
