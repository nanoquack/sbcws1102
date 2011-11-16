package sbc.dto;

public class ProductionOrder {

	private ComponentEnum component;
	private int amount;
	
	public ProductionOrder(ComponentEnum component, int amount) {
		super();
		this.component = component;
		this.amount = amount;
	}
	public ComponentEnum getComponent() {
		return component;
	}
	public void setComponent(ComponentEnum component) {
		this.component = component;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
}
