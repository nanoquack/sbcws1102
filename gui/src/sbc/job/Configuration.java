package sbc.job;

import java.io.Serializable;

import sbc.dto.CpuComponent;

public class Configuration implements Serializable{
	private static final long serialVersionUID = 450870850397910067L;
	private CpuComponent.CpuType cpuType;
	private int ramModuleCount;
	private boolean graphicsCard;

	public CpuComponent.CpuType getCpuType() {
		return cpuType;
	}

	public void setCpuType(CpuComponent.CpuType cpuType) {
		this.cpuType = cpuType;
	}

	public int getRamModuleCount() {
		return ramModuleCount;
	}

	public void setRamModuleCount(int ramModuleCount) {
		this.ramModuleCount = ramModuleCount;
	}

	public boolean isGraphicsCard() {
		return graphicsCard;
	}

	public void setGraphicsCard(boolean graphicsCard) {
		this.graphicsCard = graphicsCard;
	}
	
	@Override
	public String toString(){
		String s = "cpu type: " + getCpuType();
		s = s + "\nram mod count: " + getRamModuleCount();
		s = s + "\ngraphics card: " + isGraphicsCard();
		return s;
	}
}