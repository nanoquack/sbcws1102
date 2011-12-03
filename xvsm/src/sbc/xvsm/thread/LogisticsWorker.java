package sbc.xvsm.thread;

import java.net.URI;
import java.util.List;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.slf4j.LoggerFactory;

import sbc.INotifyGui;
import sbc.SbcConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class LogisticsWorker implements Runnable {
	private INotifyGui notifyGui;
	private Capi capi;
	private MzsCore core;
	private ContainerReference testContainer;
	private ContainerReference logisticContainer;
	private ContainerReference notficationContainer;
	private boolean running;

	public static void main(String[] args) {
		TesterWorker tester = new TesterWorker();
		Thread t = new Thread(tester);
		t.start();
		System.out.println("Logistics Worker started");
	}

	public void run() {
		try {
			// Set up local mozart space
			LoggerContext context = (LoggerContext) LoggerFactory
					.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");

			core = DefaultMzsCore.newInstance(SbcConstants.TESTERPORT);
			capi = new Capi(core);

			this.testContainer = capi.lookupContainer(
					SbcConstants.LOGISTICCONTAINER, new URI(
							SbcConstants.LOGISTICCONTAINER), 1000l, null);

			while (running) {
				List<Entry> entries = capi.take(testContainer);
				for (Entry entry : entries) {
					// TODO check if computer is ok and depending on that move
					// it to sale delivery or recycling storage
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
