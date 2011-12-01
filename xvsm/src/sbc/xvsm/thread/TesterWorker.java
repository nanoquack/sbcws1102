package sbc.xvsm.thread;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.Matchmakers;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;

import sbc.INotifyGui;
import sbc.SbcConstants;
import sbc.dto.Computer;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;

public class TesterWorker implements Runnable {

	private boolean running=true;
	private INotifyGui notifyGui;
	private Capi capi;
	private MzsCore core;
	private ContainerReference container;
	
	public static void main(String[] args){
		TesterWorker tester=new TesterWorker();
		Thread t = new Thread(tester);
		t.start();
		System.out.println("Test worker started");
	}

	public void run() {

		try{
			core = DefaultMzsCore.newInstance(SbcConstants.CONSTRUCTIONPORT);
			capi = new Capi(core);
			this.container=capi.lookupContainer(SbcConstants.CONSTRUCTIONCONTAINER, new URI(SbcConstants.LogisticContainerUrl), 1000l, null);

			while(running){
				
//				Query query=new Query();
//				Query q=query.filter(null);
				
				// Wait for a message
				FifoSelector selector=FifoCoordinator.newSelector();
				List<Selector> selectors=new ArrayList<Selector>();
				selectors.add(selector);
				//TODO: MaxEntries auf hoeheren Wert setzen
				ArrayList<Computer> resultEntries = capi.take(container, selectors, 1000, null);
				if(resultEntries.size()!=0){
					for(Computer computer:resultEntries){

						//If completness check has not yet been made, do it
						//Else: check if parts are faulty
						if(computer.getIsComplete()==null){
							computer.setIsComplete(true);
							if(computer.getCpu()==null){
								computer.setIsComplete(false);
							}if(computer.getRam()==null){
								computer.setIsComplete(false);
							}if(computer.getRam().size()==0){
								computer.setIsComplete(false);
							}
							if(computer.getMainboard()==null){
								computer.setIsComplete(false);
							}
							//TODO: Put computer back in test container
						}else if(computer.getIsComplete()==false){
							computer.setQualityCheckPassed(false);
						}else{

							computer.setQualityCheckPassed(true);
							if(computer.getCpu()==null || computer.getCpu().isFaulty()==true){
								computer.setQualityCheckPassed(false);
							}if(computer.getMainboard()==null || computer.getMainboard().isFaulty()==true){
								computer.setQualityCheckPassed(false);
							}if(computer.getGpu()!=null && computer.getGpu().isFaulty()==true){
								computer.setQualityCheckPassed(false);
							}if(computer.getRam()==null){
								computer.setQualityCheckPassed(false);
							}for(RamComponent ram:computer.getRam()){
								if(ram.isFaulty()){computer.setQualityCheckPassed(false);}
							}
							System.out.println(computer.getQualityCheckPassed()?"Computer ok":"Computer faulty");

							//TODO: Put computer into logistic container
						}
					} 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
