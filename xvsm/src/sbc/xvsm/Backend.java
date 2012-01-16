package sbc.xvsm;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import sbc.IBackend;
import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductionOrder;
import sbc.dto.RamComponent;
import sbc.dto.StorageState;
import sbc.job.Job;
import sbc.xvsm.thread.LogThread;
import sbc.xvsm.thread.ProducerThread;

public class Backend implements IBackend, NotificationListener {
	private LogThread logThread;
	// private ConstructionThread ct;
	// private TesterThread tt;
	// private LogisticThread lt;
	private int workerSequencer = 1;
	private Capi capi;
	private MzsCore core;
	private ContainerReference container;
	private ContainerReference jobsContainer;
	private ContainerReference notificationContainer;
	private ContainerReference loadbalancerContainer;
	private INotifyGui notifyGui;
	private StorageState storageState;

	public Backend() {
		// gui does startSystem() which initializes xvsm
	}

	/**
	 * Initializes XVSM and creates a container named SbcConstants.CONTAINER.
	 */
	protected void initXvsm() {
		try {
			core = DefaultMzsCore.newInstance(SbcConstants.MAINPORT
					+ SbcConstants.PRODUCERPORTOFFSET);
			System.out
					.println("construction " + core.getConfig().getSpaceUri());
			capi = new Capi(core);
			container = capi.createContainer(SbcConstants.PRODUCERCONTAINER,
					null, MzsConstants.Container.UNBOUNDED, null,
					new LindaCoordinator(), new FifoCoordinator());
			jobsContainer = capi.createContainer(SbcConstants.JOBSCONTAINER,
					null, MzsConstants.Container.UNBOUNDED, null,
					new QueryCoordinator(), new FifoCoordinator());
			NotificationManager notifManager = new NotificationManager(core);
			notifManager.createNotification(container, this, Operation.WRITE, Operation.TAKE);
			System.out.println(container.getSpace());
			
			loadbalancerContainer=capi.lookupContainer(
					SbcConstants.LOADBALANCERCONTAINER, new URI(SbcConstants.LoadBalancerUrl), MzsConstants.RequestTimeout.TRY_ONCE,null);
			Entry e=new Entry(SbcConstants.MAINPORT);
			capi.write(loadbalancerContainer,e);
			
		} catch (Exception e) {
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
		lt.setDaemon(true);
		lt.start();
		
		try{
			notificationContainer=capi.lookupContainer(SbcConstants.NOTIFICATIONCONTAINER, new URI("xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET)), MzsConstants.RequestTimeout.INFINITE, null);
		}
		catch(Exception e){
			System.err.println("Xvsm Backend: Could not lookup notification container");
		}
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
		try{
		Entry e=new Entry(SbcConstants.MAINPORT);
		capi.write(loadbalancerContainer,e);	//Log off from loadbalancer
		}catch(Exception ex){
			System.err.println("Could not log off from loadbalancer");
		}
	}

	@Override
	public void startSystem(INotifyGui notifyGui, String mainPort) {
		try {
			this.notifyGui = notifyGui;
			this.storageState = new StorageState();
			SbcConstants.MAINPORT = Integer.parseInt(mainPort);
			initXvsm();
			initializeFactory(notifyGui);
		} catch (NumberFormatException ex) {
			System.err
					.println("Given port argument is no number! XVSM not started!");
			ex.printStackTrace();
		}
	}

	@Override
	public void shutdownSystem() {
		shutDownFactory();
		try {
			Thread.sleep(1010); // Capi erst abdrehen, wenn take(1000)
								// abgelaufen ist
			System.out.println("Shutting down Xvsm...");
			// capi.destroyContainer(container, null);
			core.shutdown(true);
			System.out.println("Xvsm shutted down");
		} catch (Exception e) {
			throw new RuntimeException("Could not shutdown xvsm", e);
		}
	}

	@Override
	public void createJob(Job job) {
		try {
			capi.write(jobsContainer, new Entry(job));
			capi.write(notificationContainer,
					new Entry("Created job: " + job.toString()));
		} catch (MzsCoreException ex) {
			System.err.println("Could not write job into job container");
			ex.printStackTrace();
		}
	}

	@Override
	public void entryOperationFinished(
			org.mozartspaces.notifications.Notification source, Operation operation,
			List<? extends Serializable> entries) {
		for(Serializable entry: entries){
			Serializable component = null;
			if(entry instanceof Entry){
				component = ((Entry)entry).getValue();
			}
			else{
				component = entry;
			}
			synchronized(storageState){
				if(operation==Operation.WRITE){
					if(component instanceof CpuComponent){
						storageState.setCpu(storageState.getCpu()+1);
					}
					else if(component instanceof MainboardComponent){
						storageState.setMainboard(storageState.getMainboard()+1);
					}
					else if(component instanceof RamComponent){
						storageState.setRam(storageState.getRam()+1);
					}
					else if(component instanceof GpuComponent){
						storageState.setGpu(storageState.getGpu()+1);
					}
				}
				else if(operation==Operation.TAKE){
					if(component instanceof CpuComponent){
						storageState.setCpu(storageState.getCpu()-1);
					}
					else if(component instanceof MainboardComponent){
						storageState.setMainboard(storageState.getMainboard()-1);
					}
					else if(component instanceof RamComponent){
						storageState.setRam(storageState.getRam()-1);
					}
					else if(component instanceof GpuComponent){
						storageState.setGpu(storageState.getGpu()-1);
					}
				}
			}
		}
		notifyGui.updateStorage(storageState);
	}	
}
