package sbc.xvsm;

import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;

import sbc.IBackend;
import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.dto.ProductionOrder;
import sbc.xvsm.thread.LogThread;
import sbc.xvsm.thread.ProducerThread;
import sbc.xvsm.thread.StorageThread;

public class Backend implements IBackend {

	private StorageThread st;
	private LogThread logThread;
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
			core = DefaultMzsCore.newInstance(SbcConstants.PRODUCERPORT);
			capi = new Capi(core);
			container = capi.createContainer(
					SbcConstants.PRODUCERCONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new LindaCoordinator(), new FifoCoordinator());
			System.out.println(container.getSpace());
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
		logThread = new LogThread(notifyGui);
		Thread lt = new Thread(logThread);
		lt.start();
		st = new StorageThread(notifyGui);
		Thread storageThread = new Thread(st);
		storageThread.setDaemon(true);
		storageThread.start();
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
		logThread.stop();
		// ct.stop();
		// tt.stop();
		// lt.stop();
	}

	@Override
	public void startSystem(INotifyGui notifyGui) {
		initXvsm();
		initializeFactory(notifyGui);
	}

	@Override
	public void shutdownSystem() {
		shutDownFactory();
		try{
			Thread.sleep(1010);	//Capi erst abdrehen, wenn take(1000) abgelaufen ist
			System.out.println("Shutting down Xvsm...");
//			capi.destroyContainer(container, null);
			core.shutdown(true);
			System.out.println("Xvsm shutted down");
		}
		catch(Exception e){
			throw new RuntimeException("Could not shutdown xvsm", e);
		}
	}

}
