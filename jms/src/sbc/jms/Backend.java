package sbc.jms;

import java.util.List;

import sbc.IBackend;
import sbc.dto.ProductionOrder;
import sbc.jms.thread.ConstructionThread;
import sbc.jms.thread.LogisticThread;
import sbc.jms.thread.ProducerThread;
import sbc.jms.thread.StorageThread;
import sbc.jms.thread.TesterThread;

public class Backend implements IBackend {

	private StorageThread st;
	private ConstructionThread ct;
	private TesterThread tt;
	private LogisticThread lt;
	private int workerSequencer=1;
	
	/**
	 * Start Construction, Test and Logistic part of the factory
	 * Additionally initialize intermediate Storage for storing computer parts
	 */
	public void initializeFactory(){
		st=new StorageThread();
		Thread storageThread=new Thread(st);
		storageThread.start();
		ct=new ConstructionThread();
		Thread constructionThread=new Thread(ct);
		constructionThread.start();
		tt=new TesterThread();
		Thread testerThread=new Thread(tt);
		testerThread.start();
		lt=new LogisticThread();
		Thread logisticThread=new Thread(lt);
		logisticThread.start();
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
		ct.stop();
		tt.stop();
		lt.stop();
	}

}
