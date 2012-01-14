package sbc.job;

import java.util.UUID;

public class Job {
	private UUID uuid;
	private int quantity;
	private Configuration configuration;

	public Job(){
		this.uuid = UUID.randomUUID();
	}
	
	public Job(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
