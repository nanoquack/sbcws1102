package sbc.jms;

import sbc.IBackend;
import sbc.jms.thread.ProducerThread;
import sbc.jms.thread.StorageThread;

public class Backend implements IBackend {

	private StorageThread st;
	private int workerSequencer=1;
	
	/**
	 * Start Construction, Test and Logistic part of the factory
	 * Additionally initialize intermediate Storage for storing computer parts
	 */
	public void initializeFactory(){
		st=new StorageThread();
		Thread storageThread=new Thread(st);
		storageThread.start();
	}
	
	/**
	 * Creates new Threads with the specified parameters
	 */
	@Override
	public void createProducer(int amount, int errorRate) {
		String workername="worker"+workerSequencer++;
		
		//TODO: Produktion muss noch genauer spezifiziert werden
		//Zum Beispiel: 3 CPU, 2 GPU, 10 RAM, 5 Mainboard ==>List<ProductComponent>
		ProducerThread pt=new ProducerThread(workername,amount,errorRate);
		Thread producerThread = new Thread(pt);
		producerThread.start();
	}
	
	public void shutDownFactory(){
		st.stop();
		
	}

}
