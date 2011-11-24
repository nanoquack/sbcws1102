package sbc;

import java.util.ArrayList;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
		LoggerContext context = (LoggerContext) LoggerFactory
				.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		configurator.doConfigure("logback.xml");
		}
		catch(Exception e){
			System.out.println("error on configure logger");
		}
		
		try {
			MzsCore core = DefaultMzsCore.newInstance();
			Capi capi = new Capi(core);
			ContainerReference container = capi.createContainer();
			capi.write(container, new Entry("Hello, space!"));
			ArrayList<String> resultEntries = capi.read(container);
			System.out.println("Entry read: " + resultEntries.get(0));
			capi.destroyContainer(container, null);
			core.shutdown(true);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
