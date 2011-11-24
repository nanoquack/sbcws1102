package sbc.xvsm;

import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;

import sbc.IBackend;
import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.dto.ProductionOrder;
import sbc.xvsm.thread.ProducerThread;
import sbc.xvsm.thread.StorageThread;

public class Backend implements IBackend {

	private StorageThread st;
	// private ConstructionThread ct;
	// private TesterThread tt;
	// private LogisticThread lt;
	private int workerSequencer = 1;
	private Capi capi;
	private MzsCore core;
	private ContainerReference container;

	public Backend() {
		//gui does startSystem which initializes xvsm
//		initXvsm();
//		initializeFactory(null);
	}

	/**
	 * Initializes XVSM and creates a container named SbcConstants.CONTAINER.
	 */
	protected void initXvsm() {
		try{
			core = DefaultMzsCore.newInstance();
			capi = new Capi(core);
			container = capi.createContainer(
					SbcConstants.CONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new FifoCoordinator());
		}
		catch(Exception e){
			throw new RuntimeException("Could not initialize xvsm", e);
		}
	}

	/**
	 * Start Construction, Test and Logistic part of the factory Additionally
	 * initialize intermediate Storage for storing computer parts
	 */
	public void initializeFactory(INotifyGui notifyGui) {
		st = new StorageThread(notifyGui, capi);
		Thread storageThread = new Thread(st);
		storageThread.start();
		// ct=new ConstructionThread();
		// Thread constructionThread=new Thread(ct);
		// constructionThread.start();
		// tt=new TesterThread();
		// Thread testerThread=new Thread(tt);
		// testerThread.start();
		// lt=new LogisticThread();
		// Thread logisticThread=new Thread(lt);
		// logisticThread.start();
	}

	/**
	 * Creates new Threads with the specified parameters
	 */
	@Override
	public void createProducer(List<ProductionOrder> productionList,
			int errorRate) {
		String workername = "worker" + workerSequencer++;
		ProducerThread pt = new ProducerThread(workername, productionList,
				errorRate, capi);
		Thread producerThread = new Thread(pt);
		producerThread.start();
	}

	public void shutDownFactory() {
		st.stop();
		// ct.stop();
		// tt.stop();
		// lt.stop();
	}

	@Override
	public void startSystem() {
		initXvsm();
		initializeFactory(null);
	}

	@Override
	public void shutdownSystem() {
		try{
			capi.destroyContainer(container, null);
			core.shutdown(true);
		}
		catch(Exception e){
			throw new RuntimeException("Could not shutdown xvsm", e);
		}
	}

}
