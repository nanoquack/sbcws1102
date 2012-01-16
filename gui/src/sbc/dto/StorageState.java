package sbc.dto;

import java.io.Serializable;

/**
 * Container class for things that are kept in the storage
 * @author Sebastian Simon
 *
 */
public class StorageState implements Serializable{

	private int cpu;
	private int ram;
	private int mainboard;
	private int gpu;
	
	
	public int getCpu() {
		return cpu;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public int getRam() {
		return ram;
	}
	public void setRam(int ram) {
		this.ram = ram;
	}
	public int getMainboard() {
		return mainboard;
	}
	public void setMainboard(int mainboard) {
		this.mainboard = mainboard;
	}
	public int getGpu() {
		return gpu;
	}
	public void setGpu(int gpu) {
		this.gpu = gpu;
	}
	
    
}
