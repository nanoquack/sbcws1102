package sbc.xvsm.thread;

import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.slf4j.LoggerFactory;

import sbc.SbcConstants;
import sbc.dto.Computer;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class TesterWorker implements Runnable {

	private boolean running = true;
	private Capi capi;
	private MzsCore core;
	private ContainerReference testContainer;
	private ContainerReference logisticContainer;
	private ContainerReference notficationContainer;
	private ContainerReference productionContainer;
	private String workername;

	public static void main(String[] args) {
		TesterWorker tester = new TesterWorker();
		Thread t = new Thread(tester);
		t.start();
		System.out.println("Test worker started");
	}

	public void run() {

		workername = "tester" + new SecureRandom().nextLong();

		try {
			// Set up local mozart space
			LoggerContext context = (LoggerContext) LoggerFactory
					.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");

			try {
				core = DefaultMzsCore.newInstance(SbcConstants.TESTERPORT);
				capi = new Capi(core);

				logisticContainer = capi.createContainer(
						SbcConstants.LOGISTICCONTAINER, null,
						MzsConstants.Container.UNBOUNDED, null,
						new FifoCoordinator());
			} catch (MzsCoreRuntimeException ex) {
				// There exits yet another tester, logisticContainer is already
				// created
				core = DefaultMzsCore.newInstance(0);
				capi = new Capi(core);
				this.logisticContainer = capi.lookupContainer(
						SbcConstants.LOGISTICCONTAINER, new URI(
								SbcConstants.LogisticContainerUrl),
						MzsConstants.RequestTimeout.INFINITE, null);
			}

			this.notficationContainer = capi.lookupContainer(
					SbcConstants.NOTIFICATIONCONTAINER, new URI(
							SbcConstants.NotificationUrl),
					MzsConstants.RequestTimeout.INFINITE, null);
			this.testContainer = capi.lookupContainer(
					SbcConstants.TESTERCONTAINER, new URI(
							SbcConstants.TesterContainerUrl),
					MzsConstants.RequestTimeout.INFINITE, null);
			this.productionContainer = capi.lookupContainer(
					SbcConstants.PRODUCERCONTAINER, new URI(
							SbcConstants.ProducerUrl),
					MzsConstants.RequestTimeout.INFINITE, null);
			// <Testdaten>
			// this.testContainer = capi.createContainer(null, null,
			// MzsConstants.Container.UNBOUNDED,
			// Arrays.asList(new QueryCoordinator()), null, null);
			// Computer computer1=new Computer();
			// computer1.setCompletenessTester("abc");
			// Entry entry1=new Entry(computer1);
			// capi.write(testContainer, entry1);
			// Computer computer2=new Computer();
			// computer2.setCompletenessTester(workername);
			// Entry entry2=new Entry(computer2);
			// capi.write(testContainer, entry2);
			// Computer computer3=new Computer();
			// Entry entry3=new Entry(computer3);
			// capi.write(testContainer, entry3);
			// System.out.println(computer1.getCompletenessTester());
			// System.out.println(computer2.getCompletenessTester());
			// System.out.println(computer3.getCompletenessTester());
			// System.out.println("--------------------");
			// </Testdaten>

			Property title = Property.forName("*", "completenessTester");
			Query query = new Query().filter(title.notEqualTo(workername));

			System.out.println("Setup complete");

			while (running) {
				// Take a computer for testing, that has not been tested yet by
				// this tester
				ArrayList<Computer> resultEntries = capi.take(testContainer,
						Arrays.asList(QueryCoordinator.newSelector(query)),
						MzsConstants.RequestTimeout.INFINITE, null);

				// Wait for a message
				for (Computer computer : resultEntries) {
					// If completness check has not yet been made, do it
					// Else: check if parts are faulty
					if (computer.getIsComplete() == null) {
						completenessTest(computer);
					} else {
						// if computer does pass quality check, write it into
						// logistics container, otherwise disassemble it and
						// write the functioning components into production
						// container
						if (qualityCheck(computer)) {
							Entry e = new Entry(computer);
							capi.write(logisticContainer, e);
						} else {
							disassemble(computer);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean completenessTest(Computer computer) throws MzsCoreException {
		computer.setIsComplete(true);
		if (computer.getCpu() == null) {
			computer.setIsComplete(false);
		}
		if (computer.getRam() == null) {
			computer.setIsComplete(false);
		} else if (computer.getRam().size() == 0) {
			computer.setIsComplete(false);
		}
		if (computer.getMainboard() == null) {
			computer.setIsComplete(false);
		}

		computer.setCompletenessTester(workername);
		// If the computer is complete==>put it back into
		// testing container
		if (computer.getIsComplete()) {
			Entry e = new Entry(computer);
			capi.write(testContainer, e);
			System.out
					.println("Computer complete, put it back into test container");
		} else { // Else: set checkPassed=false and forward to
					// logistic
			computer.setQualityCheckPassed(false);
			Entry e = new Entry(computer);
			capi.write(logisticContainer, e);
			capi.write(notficationContainer,
					new Entry("Computer is incomplete"));
			System.out
					.println("Computer incomplete. Quality check failed. Forward it to logistic");
		}

		return computer.getIsComplete();
	}

	/**
	 * Checks if any components of a computer are faulty, sets the quality check
	 * passed accordingly and returns it.
	 * 
	 * @param computer
	 *            The computer to check for faulty components
	 * @return true if quality check was passed, false otherwise.
	 * @throws MzsCoreException
	 */
	private boolean qualityCheck(Computer computer) throws MzsCoreException {
		computer.setQualityCheckPassed(true);
		if (computer.getCpu() == null || computer.getCpu().isFaulty() == true) {
			computer.setQualityCheckPassed(false);
		}
		if (computer.getMainboard() == null
				|| computer.getMainboard().isFaulty() == true) {
			computer.setQualityCheckPassed(false);
		}
		if (computer.getGpu() != null && computer.getGpu().isFaulty() == true) {
			computer.setQualityCheckPassed(false);
		}
		if (computer.getRam() == null) {
			computer.setQualityCheckPassed(false);
		}
		for (RamComponent ram : computer.getRam()) {
			if (ram.isFaulty()) {
				computer.setQualityCheckPassed(false);
			}
		}
		System.out.println(computer.getQualityCheckPassed() ? "Computer ok"
				: "Computer faulty");
		capi.write(notficationContainer,
				new Entry(computer.getQualityCheckPassed() ? "Computer ok"
						: "Computer faulty"));

		return computer.getQualityCheckPassed();
	}

	/**
	 * Disassembles a computer and rewrite the components into the containers.
	 * 
	 * @param computer
	 */
	private void disassemble(Computer computer) throws MzsCoreException {
		ProductComponent component = null;
		capi.write(notficationContainer,
				new Entry("Disassembling Computer"));
		if (!computer.getCpu().isFaulty()) {
			capi.write(productionContainer, new Entry(computer.getCpu()));
			capi.write(notficationContainer,
					new Entry("Functioning Cpu added to production"));
		}
		if ((computer.getGpu() != null) && (!computer.getGpu().isFaulty())) {
			capi.write(productionContainer, new Entry(computer.getGpu()));
			capi.write(notficationContainer,
					new Entry("Functioning Gpu added to production"));
		}
		if (!computer.getMainboard().isFaulty()) {
			capi.write(productionContainer, new Entry(computer.getMainboard()));
			capi.write(notficationContainer,
					new Entry("Functioning Mainboard added to production"));
		}

		for (ProductComponent ram : computer.getRam()) {
			capi.write(productionContainer, new Entry(ram));
			capi.write(notficationContainer,
					new Entry("Functioning Ram added to production"));
		}
	}
}
