package sbc.loadbalancing;

import java.util.ArrayList;

import org.mozartspaces.core.ContainerReference;

import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.RamComponent;

public class FactoryReference {

	private int port;
	private ContainerReference productionContainer;
	private ContainerReference ordersContainer;
	
	private ArrayList<CpuComponent> cpuComponents;
	private ArrayList<MainboardComponent> mainboardComponents;
	private ArrayList<RamComponent> ramComponents;
	private ArrayList<GpuComponent> gpuComponents;
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public ContainerReference getProductionContainer() {
		return productionContainer;
	}
	public void setProductionContainer(ContainerReference productionContainer) {
		this.productionContainer = productionContainer;
	}
	public ContainerReference getOrdersContainer() {
		return ordersContainer;
	}
	public void setOrdersContainer(ContainerReference ordersContainer) {
		this.ordersContainer = ordersContainer;
	}
	public ArrayList<CpuComponent> getCpuComponents() {
		return cpuComponents;
	}
	public void setCpuComponents(ArrayList<CpuComponent> cpuComponents) {
		this.cpuComponents = cpuComponents;
	}
	public ArrayList<MainboardComponent> getMainboardComponents() {
		return mainboardComponents;
	}
	public void setMainboardComponents(
			ArrayList<MainboardComponent> mainboardComponents) {
		this.mainboardComponents = mainboardComponents;
	}
	public ArrayList<RamComponent> getRamComponents() {
		return ramComponents;
	}
	public void setRamComponents(ArrayList<RamComponent> ramComponents) {
		this.ramComponents = ramComponents;
	}
	public ArrayList<GpuComponent> getGpuComponents() {
		return gpuComponents;
	}
	public void setGpuComponents(ArrayList<GpuComponent> gpuComponents) {
		this.gpuComponents = gpuComponents;
	}
	
	
}
