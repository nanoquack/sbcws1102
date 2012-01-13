package sbc.dto;

import java.io.Serializable;

import org.mozartspaces.capi3.Queryable;

@Queryable
public abstract class ProductComponent implements Serializable {

	private static final long serialVersionUID = -1073560758968262746L;
	private Integer id;  //Only unique in combination with worker name
	private String worker;
	private Boolean faulty;
	
	public ProductComponent(Integer id, String worker, Boolean faulty){
		this.id=id;
		this.worker=worker;
		this.faulty=faulty;
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
	public boolean isFaulty() {
		return faulty;
	}
	public void setFaulty(boolean faulty) {
		this.faulty = faulty;
	}
	
	
	@Override
	public String toString(){
		String s = "class: " + getClass().getCanonicalName();
		s = s + "\n id: " + getId();
		s = s + "\n worker: " + getWorker();
		s = s + "\nfaulty: " + Boolean.toString(faulty);
		
		return s;
	}
}
