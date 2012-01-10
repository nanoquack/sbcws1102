package sbc.loadbalancing;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.core.ContainerReference;

import sbc.dto.ComponentEnum;
import sbc.dto.ProductionOrder;

public class LoadBalancer implements Runnable {

	//List of all demanded components
//	private List<ProductionOrder> orderList;

	private ContainerReference storage;
	
	//TODO:
	//private List<Order> orders;  
	
	//TODO: Obtain reference of other fabrics
	private ArrayList<ContainerReference> partnerFabrics;

	private boolean running=true;

	/**
	 * Local fabric adds demanded components to the load balancer
	 * If the amount of demanded components of a component type
	 * exceeds a certain threshold, the load balancers tries
	 * to obtain these components from other fabrics
	 */
//	public void updateDemandUnits(List<ProductionOrder> orderList){
//		this.orderList=orderList;
//	}

	/**
	 * Transfer units from the local fabric to another
	 */
	public void transferUnits(){

	}

	/**
	 * Check regularly if components should be transfered
	 * Components are transfered, if a certain limit has been exceeded
	 * All needed information is extracted from the containers and
	 * the current list of orders.
	 */
	@Override
	public void run() {
		try{
			while(running){
				Thread.sleep(1000);
				//
			}
		}
		catch(InterruptedException ex){
			System.err.println("Error in LoadBalancer-Thread occured");
			ex.printStackTrace();
		}
	}

	public void stop(){
		this.running=false;
	}

}
