package sbc.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

/**
 * Encapsulates components and attributes of a computer
 */
@Queryable
public class Computer implements Serializable{

	private CpuComponent cpu;
	private MainboardComponent mainboard;
	private GpuComponent gpu;
	private List<RamComponent> ram;
	private Boolean isComplete;
	@Index  //If computer is complete, the name of the tester is written into this field
	private String completenessTester;	
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
		isComplete=null;
		completenessTester=null;
		qualityCheckPassed=null;
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

	public Boolean getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(Boolean isComplete) {
		this.isComplete = isComplete;
	}

	public String getCompletenessTester() {
		return completenessTester;
	}

	public void setCompletenessTester(String completenessTester) {
		this.completenessTester = completenessTester;
	}

	public void addRam(RamComponent comp){
		if(ram==null){
			ram=new ArrayList<RamComponent>();
		}
		ram.add(comp);
	}
	
	@Override
	public String toString(){
		String s = "--COMPUTER--";
		s = s + "Quality check passed: " + getQualityCheckPassed().toString();
		s = s + "\n Mainboard: \n" + getMainboard().toString();
		s = s + "\n Cpu: \n" + getCpu().toString();
		for(RamComponent r: ram){
			s = s + "\n RamComponent: \n" + r.toString();
		}
		if(getGpu()!=null){
			s = s + "\nGpu: \n" + getGpu().toString();
		}
		s = s + "\n------------";
		
		return s;
	}
}
