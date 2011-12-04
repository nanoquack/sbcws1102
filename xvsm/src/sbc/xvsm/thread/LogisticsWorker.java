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
import sbc.dto.Computer;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class LogisticsWorker implements Runnable {
	private Capi capi;
	private MzsCore core;
	private ContainerReference logisticContainer;
	private ContainerReference salesContainer;
	private ContainerReference recyclingContainer;
	private ContainerReference notficationContainer;
	private boolean running;

	public static void main(String[] args) {
		LogisticsWorker logistics = new LogisticsWorker();
		Thread t = new Thread(logistics);
		t.start();
		System.out.println("Logistics Worker started");
	}

	public LogisticsWorker() {
		this.running = true;
	}

	public void run() {
		try {
			initXvsm();

			while (running) {
				List<Computer> entries = capi.take(logisticContainer,
						Arrays.asList(FifoCoordinator.newSelector()),
						MzsConstants.RequestTimeout.INFINITE, null);
				for (Computer computer : entries) {
					if (computer != null) {
						if (computer.getQualityCheckPassed()) {
							capi.write(salesContainer, new Entry(computer));
							String logMsg = logMsg = "Computer has passed quality check, goes to sales: \n" + computer.toString();
							capi.write(
									notficationContainer,
									new Entry(
											logMsg));
						} else {
							capi.write(recyclingContainer, new Entry(computer));
							String logMsg = logMsg = "Computer has NOT passed quality check, goes to recycling: \n" + computer.toString();
							capi.write(
									notficationContainer,
									new Entry(
											logMsg));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				core = DefaultMzsCore.newInstance(SbcConstants.LOGISTICPORT);
				capi = new Capi(core);
				salesContainer = capi.createContainer(
						SbcConstants.SALESCONTAINER, null,
						MzsConstants.Container.UNBOUNDED, null,
						new FifoCoordinator());
				recyclingContainer = capi.createContainer(
						SbcConstants.RECYCLINGCONTAINER, null,
						MzsConstants.Container.UNBOUNDED, null,
						new FifoCoordinator());
			} catch (MzsCoreRuntimeException e) {
				System.err
						.println("Default port for LogisticsWorker is already taken, using arbitrary port");
				core = DefaultMzsCore.newInstance(0);
				capi = new Capi(core);
				salesContainer = capi.lookupContainer(
						SbcConstants.SALESCONTAINER, new URI(
								SbcConstants.SalesContainerUrl),
						MzsConstants.RequestTimeout.INFINITE, null);
				recyclingContainer = capi.lookupContainer(
						SbcConstants.RECYCLINGCONTAINER, new URI(
								SbcConstants.RecyclingContainerUrl),
						MzsConstants.RequestTimeout.INFINITE, null);
			}
			logisticContainer = capi.lookupContainer(
					SbcConstants.LOGISTICCONTAINER, new URI(
							SbcConstants.LogisticContainerUrl),
					MzsConstants.RequestTimeout.INFINITE, null);
			notficationContainer = capi.lookupContainer(
					SbcConstants.NOTIFICATIONCONTAINER, new URI(
							SbcConstants.NotificationUrl),
					MzsConstants.RequestTimeout.INFINITE, null);
		} catch (Exception e) {
			System.err.println("Could not inintialize Xvsm");
		}
	}
}
