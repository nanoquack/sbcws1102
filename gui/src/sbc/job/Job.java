package sbc.job;

public class Job {
	private int quantity;
	private Configuration configuration;

	public Job(Configuration configuration) {
		this.configuration = configuration;
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
