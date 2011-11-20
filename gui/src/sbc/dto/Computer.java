package sbc.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates components and attributes of a computer
 */
public class Computer implements Serializable{

	private CpuComponent cpu;
	private MainboardComponent mainboard;
	private GpuComponent gpu;
	private List<RamComponent> ram;
	private Boolean qualityCheckPassed;

	public Computer(){
		this.qualityCheckPassed=false;
	}
	
	public Computer(CpuComponent cpu, MainboardComponent mainboard,
			GpuComponent gpu, List<RamComponent> ram) {
		super();
		this.cpu = cpu;
		this.mainboard = mainboard;
		this.gpu = gpu;
		this.ram = ram;
		qualityCheckPassed=false;
	}

	public CpuComponent getCpu() {
		return cpu;
	}

	public void setCpu(CpuComponent cpu) {
		this.cpu = cpu;
	}

	public MainboardComponent getMainboard() {
		return mainboard;
	}

	public void setMainboard(MainboardComponent mainboard) {
		this.mainboard = mainboard;
	}

	public GpuComponent getGpu() {
		return gpu;
	}

	public void setGpu(GpuComponent gpu) {
		this.gpu = gpu;
	}

	public List<RamComponent> getRam() {
		return ram;
	}

	public void setRam(List<RamComponent> ram) {
		this.ram = ram;
	}

	public Boolean getQualityCheckPassed() {
		return qualityCheckPassed;
	}

	public void setQualityCheckPassed(Boolean qualityCheckPassed) {
		this.qualityCheckPassed = qualityCheckPassed;
	}
	
	public void addRam(RamComponent comp){
		if(ram==null){
			ram=new ArrayList<RamComponent>();
		}
		ram.add(comp);
	}

}
