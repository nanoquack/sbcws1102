package sbc.jms;

import java.util.List;

import sbc.IBackend;
import sbc.INotifyGui;
import sbc.dto.ProductionOrder;
import sbc.jms.thread.ConstructionWorker;
import sbc.jms.thread.LogisticWorker;
import sbc.jms.thread.ProducerThread;
import sbc.jms.thread.StorageThread;
import sbc.jms.thread.TesterWorker;

public class Backend implements IBackend {

	private StorageThread st;
	private ConstructionWorker ct;
	private TesterWorker tt;
	private LogisticWorker lt;
	private int workerSequencer=1;
	
	/**
	 * Start Construction, Test and Logistic part of the factory
	 * Additionally initialize intermediate Storage for storing computer parts
	 */
	public void initializeFactory(INotifyGui notifyGui){
		st=new StorageThread(notifyGui);
		Thread storageThread=new Thread(st);
		storageThread.start();
	}
	
	/**
	 * Creates new Threads with the specified parameters
	 */
	@Override
	public void createProducer(List<ProductionOrder> productionList, int errorRate) {
		String workername="worker"+workerSequencer++;
		ProducerThread pt=new ProducerThread(workername,productionList,errorRate);
		Thread producerThread = new Thread(pt);
		producerThread.start();
	}
	
	public void shutDownFactory(){
		st.stop();
	}

	@Override
	public void startSystem(INotifyGui notifyGui) {
		initializeFactory(notifyGui);
	}

	@Override
	public void shutdownSystem() {
		shutDownFactory();
		
	}

}
