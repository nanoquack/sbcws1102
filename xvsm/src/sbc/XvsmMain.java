package sbc;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import sbc.dto.ComponentEnum;
import sbc.dto.ProductionOrder;
import sbc.gui.MainFrame;
import sbc.xvsm.Backend;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class XvsmMain {

	/**
	 * @param args
	 */
	@Deprecated
	public static void main(String[] args) {
		try {
			System.out.println("Main Port:" +args.length);
			if(args.length!=1){
				System.err.println("Main Port has to be specified");
				System.exit(1);
			}
			SbcConstants.MAINPORT=Integer.parseInt(args[0]);
			
			LoggerContext context = (LoggerContext) LoggerFactory
					.getILoggerFactory();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure("logback.xml");
			Backend backend=new Backend();
			MainFrame frame = new MainFrame();
			backend.startSystem(frame, args[0]);
			frame.setBackend(backend);
			frame.setVisible(true);
			
//			List<ProductionOrder> productionList=new ArrayList<ProductionOrder>();
//			productionList.add(new ProductionOrder(ComponentEnum.CPU,2));
//			productionList.add(new ProductionOrder(ComponentEnum.MAINBOARD,2));
//			productionList.add(new ProductionOrder(ComponentEnum.RAM,6));
//			productionList.add(new ProductionOrder(ComponentEnum.GPU,1));
//			backend.createProducer(productionList,10);
//			Thread.sleep(10);
//
//			List<ProductionOrder> productionList2=new ArrayList<ProductionOrder>();
//			productionList2.add(new ProductionOrder(ComponentEnum.CPU,1));
//			productionList2.add(new ProductionOrder(ComponentEnum.MAINBOARD,1));
//			productionList2.add(new ProductionOrder(ComponentEnum.RAM,2));
//			productionList2.add(new ProductionOrder(ComponentEnum.GPU,1));
//			
//			backend.createProducer(productionList2,10);
			
//			Thread.sleep(10000);
//			backend.shutdownSystem();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
