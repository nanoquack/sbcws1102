package sbc;

import java.util.List;

import sbc.dto.ProductionOrder;

/**
 * Defines the methods that the backend has to supply
 */
public interface IBackend {
	
	/**
	 * 
	 * @param list of production units to produce
	 * @param errorRate of producers
	 */
	public void createProducer(List<ProductionOrder> productionList, int errorRate);

}
