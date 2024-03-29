package sbc.xvsm.thread;

import java.net.URI;
import java.util.List;
import java.util.Random;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;

import sbc.SbcConstants;
import sbc.dto.ComponentEnum;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.ProductionOrder;
import sbc.dto.RamComponent;

public class ProducerThread implements Runnable {

	private List<ProductionOrder> productionList;
	private int errorRate;
	private String workername;
	private int productSequencer=1;
	private Capi capi;
	private ContainerReference container;
	private ContainerReference notificationContainer;

	public ProducerThread(String workername, List<ProductionOrder> productionList, 
			int errorRate, Capi capi){
		this.workername=workername;
		this.productionList=productionList;
		this.errorRate=errorRate;
		this.capi=capi;
	}

	@Override
	public void run() {
		try{
			this.container=capi.lookupContainer(
					SbcConstants.PRODUCERCONTAINER, new URI(
							"xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)),
					MzsConstants.RequestTimeout.INFINITE, null);	
			this.notificationContainer = capi.lookupContainer(
					SbcConstants.NOTIFICATIONCONTAINER, new URI(
							"xvsm://localhost:"+(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET)),
					MzsConstants.RequestTimeout.INFINITE, null);	
			capi.write(notificationContainer, new Entry("ProductionWorker: Setup complete, port: "+(SbcConstants.MAINPORT+SbcConstants.PRODUCERPORTOFFSET)));
			for (ProductionOrder order : productionList){
				int amount=order.getAmount();
				while(amount>0){
					Random rand=new Random();
					int randomTime=rand.nextInt(3)+1;
					Thread.sleep(randomTime*10);	//TODO: 1000
					double randomFaulty=rand.nextDouble();
					boolean faulty=randomFaulty<=(errorRate/100.0);
					Entry entry=null;
					// Create a messages
					switch(order.getComponent()){
						case CPU:
							entry=new Entry(new CpuComponent(productSequencer++,workername,faulty));
							break;
						case RAM:
							entry=new Entry(new RamComponent(productSequencer++,workername,faulty));
							break;
						case MAINBOARD:
							entry=new Entry(new MainboardComponent(productSequencer++,workername,faulty));
							break;
						case GPU:
							entry=new Entry(new GpuComponent(productSequencer++,workername,faulty));
							break;
						default:
							throw new RuntimeException();
					}

					// Put entry with compoent into the container
					capi.write(container, entry);
					ProductComponent component = (ProductComponent)entry.getValue();
					capi.write(notificationContainer, new Entry("produced new component: "+component.getClass().getCanonicalName() + ", id: " + component.getId()));
					
					amount--;
				}
			}

		} catch (Exception e) {
			System.out.println("ProducerThread - error while interaction with MozartSpaces.");
			e.printStackTrace();
		}

	}

}
