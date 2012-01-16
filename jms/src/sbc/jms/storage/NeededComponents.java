package sbc.jms.storage;

import java.io.Serializable;

public class NeededComponents implements Serializable {
	
	
	private static final long serialVersionUID = 5251301713878138565L;
	private String factoryId;
	private int ramsNeeded;
	private int mainboardsNeeded;
	private int gpusNeeded;
	private int singleCore16sNeeded;
	private int dualCore02sNeeded;
	private int dualCore24sNeeded;
	


	public NeededComponents(String factoryId, int ramsNeeded,
			int mainboardsNeeded, int gpusNeeded, int singleCore16sNeeded,
			int dualCore02sNeeded, int dualCore24sNeeded) {
		super();
		this.factoryId = factoryId;
		this.ramsNeeded = ramsNeeded;
		this.mainboardsNeeded = mainboardsNeeded;
		this.gpusNeeded = gpusNeeded;
		this.singleCore16sNeeded = singleCore16sNeeded;
		this.dualCore02sNeeded = dualCore02sNeeded;
		this.dualCore24sNeeded = dualCore24sNeeded;
	}

	public int getRamsNeeded() {
		return ramsNeeded;
	}

	public void setRamsNeeded(int ramsNeeded) {
		this.ramsNeeded = ramsNeeded;
	}

	public int getMainboardsNeeded() {
		return mainboardsNeeded;
	}

	public void setMainboardsNeeded(int mainboardsNeeded) {
		this.mainboardsNeeded = mainboardsNeeded;
	}

	public int getGpusNeeded() {
		return gpusNeeded;
	}

	public void setGpusNeeded(int gpusNeeded) {
		this.gpusNeeded = gpusNeeded;
	}

	public int getSingleCore16sNeeded() {
		return singleCore16sNeeded;
	}

	public void setSingleCore16sNeeded(int singleCore16sNeeded) {
		this.singleCore16sNeeded = singleCore16sNeeded;
	}

	public int getDualCore02sNeeded() {
		return dualCore02sNeeded;
	}

	public void setDualCore02sNeeded(int dualCore02sNeeded) {
		this.dualCore02sNeeded = dualCore02sNeeded;
	}

	public int getDualCore24sNeeded() {
		return dualCore24sNeeded;
	}

	public void setDualCore24sNeeded(int dualCore24sNeeded) {
		this.dualCore24sNeeded = dualCore24sNeeded;
	}

	public String getFactoryId() {
		return factoryId;
	}

	public void setFactoryId(String factoryId) {
		this.factoryId = factoryId;
	}
	
	
	
}
