package sbc.dto;

import java.io.Serializable;

public class ProductComponent implements Serializable {

	private int id;  //Only unique in combination with worker name
	private String worker;
	private ComponentEnum component;
	private boolean faulty;
	
	public ProductComponent(int id, String worker, ComponentEnum component){
		this.id=id;
		this.worker=worker;
		this.component=component;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getWorker() {
		return worker;
	}
	public void setWorker(String worker) {
		this.worker = worker;
	}
	public ComponentEnum getComponent() {
		return component;
	}
	public void setComponent(ComponentEnum component) {
		this.component = component;
	}
	
	
}
