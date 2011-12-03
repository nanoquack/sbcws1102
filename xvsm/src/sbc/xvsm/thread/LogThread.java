package sbc.xvsm.thread;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.slf4j.LoggerFactory;

import sbc.INotifyGui;
import sbc.SbcConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class LogThread implements Runnable {
	private INotifyGui notifyGui;
	private boolean running;
	private Capi capi;
	private MzsCore core;
	private ContainerReference notificationContainer;

	public LogThread(INotifyGui notifyGui) {
		this.notifyGui = notifyGui;
		this.running = true;
	}

	@Override
	public void run() {
		initXvsm();
		try{
			capi.write(notificationContainer, new Entry("Xvsm LogThread started"));
		}
		catch(Exception e){
			notifyGui.addLogMessage("Could not start Xvsm LogThread");
		}
		
		while(running){
			try{
				List<String> logEntries = capi.take(notificationContainer, Arrays.asList(FifoCoordinator.newSelector()), MzsConstants.RequestTimeout.INFINITE, null);
				for(String logEntry: logEntries){
					notifyGui.addLogMessage(logEntry);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void initXvsm() {
		try {
			LoggerContext context = (LoggerContext) LoggerFactory
					.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");

			try {
				core = DefaultMzsCore.newInstance(SbcConstants.LOGGERPORT);
				capi = new Capi(core);
//				notificationContainer = capi.lookupContainer(
//						SbcConstants.NOTIFICATIONCONTAINER, new URI(
//								SbcConstants.NotificationUrl),
//						MzsConstants.RequestTimeout.INFINITE, null);
				notificationContainer = capi.createContainer(
						SbcConstants.NOTIFICATIONCONTAINER, null, MzsConstants.Container.UNBOUNDED,
						null, new FifoCoordinator());
			} catch (MzsCoreRuntimeException e) {
				System.err.println("A LogThread is already running on port "+SbcConstants.LOGGERPORT);
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.err.println("Could not inintialize Xvsm");
			e.printStackTrace();
		}
	}
}
