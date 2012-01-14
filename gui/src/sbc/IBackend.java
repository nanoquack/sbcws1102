package sbc;

import java.util.List;

import sbc.dto.ProductionOrder;
import sbc.job.Configuration;
import sbc.job.Job;

/**
 * Defines the methods that the backend has to supply
 */
public interface IBackend {
	
	/**
	 * Initializes and starts everything the backend service needs to function.  
	 */
	public void startSystem(INotifyGui notifyGui, String factoryInfo);
	
	/**
	 * Shuts the system down correctly and releases all resources.  
	 */
	public void shutdownSystem();
	
	/**
	 * 
	 * @param list of production units to produce
	 * @param errorRate of producers
	 */
	public void createProducer(List<ProductionOrder> productionList, int errorRate);
	
	public void createJob(Job job);

}
