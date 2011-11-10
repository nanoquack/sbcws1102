package sbc;

/**
 * Defines the methods that the backend has to supply
 */
public interface IBackend {
	
	/**
	 * 
	 * @param amount of producers
	 * @param errorRate of producers
	 */
	public void createProducer(int amount, int errorRate);

}
