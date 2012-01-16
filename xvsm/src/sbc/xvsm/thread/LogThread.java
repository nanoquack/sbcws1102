package sbc.xvsm.thread;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;
import org.slf4j.LoggerFactory;

import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.job.Job;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogThread implements Runnable, NotificationListener {
	private INotifyGui notifyGui;
	private Capi capi;
	private MzsCore core;
	private ContainerReference notificationContainer;

	public LogThread(INotifyGui notifyGui) {
		this.notifyGui = notifyGui;
		initXvsm();
	}

	@Override
	public void run() {
		try{
			capi.write(notificationContainer, new Entry("Xvsm LogThread started"));
		}
		catch(Exception e){
			notifyGui.addLogMessage("Could not start Xvsm LogThread");
		}
	}

	private void initXvsm() {
		try {
			try {
				configureXvsmLogging();
				
				core = DefaultMzsCore.newInstance(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET);
				capi = new Capi(core);
				notificationContainer = capi.createContainer(
						SbcConstants.NOTIFICATIONCONTAINER, null, MzsConstants.Container.UNBOUNDED,
						null, new FifoCoordinator());
			} catch (MzsCoreRuntimeException e) {
				System.out.println("A LogThread is already running on port "+SbcConstants.LOGGERPORTOFFSET);
				core = DefaultMzsCore.newInstance(0);
				capi = new Capi(core);
				notificationContainer = capi.lookupContainer(
						SbcConstants.NOTIFICATIONCONTAINER, new URI(
								"xvsm://localhost:+"+(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET)),
						MzsConstants.RequestTimeout.INFINITE, null);				
			}
			NotificationManager notifManager = new NotificationManager(core);
			notifManager.createNotification(notificationContainer, this, Operation.WRITE);
		} catch (Exception e) {
			System.err.println("Could not inintialize Xvsm");
			e.printStackTrace();
		}
	}
	
	private void configureXvsmLogging() throws JoranException{
		LoggerContext context = (LoggerContext) LoggerFactory
				.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure("logback.xml");
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		for(Serializable logEntry: entries){
			if(logEntry instanceof Entry){
				Object value = ((Entry)logEntry).getValue();
				if(value instanceof String){
					notifyGui.addLogMessage((String)value);
				}
				//a job was finished, so tell the gui
				if(value instanceof Job){
					Job job = (Job)value;
					if(job.getQuantity()>0){
						notifyGui.addJob(job);
					}
					else{
						notifyGui.removeJob(job);
					}
				}
			}
		}
	}
}
