package sbc.job;

import sbc.dto.CpuComponent;

public class Configuration {
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
}