package sbc.xvsm.thread;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.slf4j.LoggerFactory;

import sbc.SbcConstants;
import sbc.dto.Computer;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class ConstructionWorker implements Runnable {

	private boolean running=true;
	private Capi capi;
	private MzsCore core;
	private ContainerReference storageContainer;
	private ContainerReference testContainer;
	private ContainerReference notficationContainer;
//	private String workername;

	public static void main(String[] args){
		ConstructionWorker constructor=new ConstructionWorker();
		Thread t = new Thread(constructor);
		t.start();
		System.out.println("Construction worker started");
	}

	public void run() {

//		workername="tester"+new SecureRandom().nextLong();

		try{
			//Set up local mozart space
			LoggerContext context = (LoggerContext) LoggerFactory
			.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");

			try{
			core = DefaultMzsCore.newInstance(SbcConstants.CONSTRUCTIONPORT);
			capi = new Capi(core);
			
			testContainer = capi.createContainer(
					SbcConstants.TESTERCONTAINER, null, MzsConstants.Container.UNBOUNDED,
					null, new QueryCoordinator());
			}catch(MzsCoreRuntimeException ex){
				//There exits yet another tester, logisticContainer is already created
				core = DefaultMzsCore.newInstance(0);
				capi = new Capi(core);
				this.testContainer=capi.lookupContainer(SbcConstants.TESTERCONTAINER, new URI(SbcConstants.TesterContainerUrl), MzsConstants.RequestTimeout.INFINITE, null);
			}
			this.notficationContainer=capi.lookupContainer(SbcConstants.NOTIFICATIONCONTAINER, new URI(SbcConstants.NotificationUrl), MzsConstants.RequestTimeout.INFINITE, null);
			this.storageContainer=capi.lookupContainer(SbcConstants.STORAGECONTAINER, new URI(SbcConstants.StorageContainerUrl), MzsConstants.RequestTimeout.INFINITE, null);			

			List<Selector> selectors=new ArrayList<Selector>();
			selectors.add(FifoCoordinator.newSelector());

			System.out.println("Setup complete");
			while(running){
				//Take a computer for testing, that has not been tested yet by this tester
				ArrayList<ArrayList<ProductComponent>> resultEntries = capi.take(storageContainer, selectors, MzsConstants.RequestTimeout.INFINITE, null);

				// Wait for a message
				for(List<ProductComponent> components:resultEntries){
					Computer computer=new Computer();
					while(components.size()!=0){
						ProductComponent comp=components.remove(0);
						if(comp instanceof CpuComponent){
							computer.setCpu((CpuComponent)comp);
						}if(comp instanceof MainboardComponent){
							computer.setMainboard((MainboardComponent)comp);
						}if(comp instanceof GpuComponent){
							computer.setGpu((GpuComponent)comp);
						}if(comp instanceof RamComponent){
							computer.addRam((RamComponent)comp);
						}
					}
					Entry e=new Entry(computer);
					capi.write(testContainer, e);
					capi.write(notficationContainer, new Entry("New computer constructed"));
					System.out.println("New computer constructed");
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

