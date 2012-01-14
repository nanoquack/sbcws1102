package sbc.loadbalancing;

import java.util.ArrayList;

import org.mozartspaces.core.ContainerReference;

import sbc.dto.CpuComponent;
import sbc.dto.CpuComponent.CpuType;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.RamComponent;
import sbc.job.Job;

public class FactoryReference {

	private int port;
	private ContainerReference productionContainer;
	private ContainerReference jobsContainer;
	
	private ArrayList<CpuComponent> cpuComponents;
	private ArrayList<MainboardComponent> mainboardComponents;
	private ArrayList<RamComponent> ramComponents;
	private ArrayList<GpuComponent> gpuComponents;
	
	private ArrayList<Job> jobs;
	
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
	public ContainerReference getJobsContainer() {
		return jobsContainer;
	}
	public void setJobsContainer(ContainerReference ordersContainer) {
		this.jobsContainer = ordersContainer;
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
	public ArrayList<Job> getJobs() {
		return jobs;
	}
	public void setJobs(ArrayList<Job> jobs) {
		this.jobs = jobs;
	}
	
	public int getNumberOfRams(){
		return ramComponents.size();
	}
	
	public int getNumberOfMainboards(){
		return mainboardComponents.size();
	}
	
	public int getNumberOfGpus(){
		return gpuComponents.size();
	}
	
	public int getNumberOfSingleCore16CPU(){
		int i=0;
		for(CpuComponent cpu:cpuComponents){
			if(cpu.getCpuType().equals(CpuType.SINGLE_CORE_16)){
				i++;
			}
		}
		return i;
	}
	public int getNumberOfDualCore02CPU(){
		int i=0;
		for(CpuComponent cpu:cpuComponents){
			if(cpu.getCpuType().equals(CpuType.DUAL_CORE_2)){
				i++;
			}
		}
		return i;
	}
	
	public int getNumberOfDualCore24CPU(){
		int i=0;
		for(CpuComponent cpu:cpuComponents){
			if(cpu.getCpuType().equals(CpuType.DUAL_CORE_24)){
				i++;
			}
		}
		return i;
	}
	
	public int getNumberOfMainboardsNeeded(){
		int i=0;
		for(Job job:jobs){
			i+=job.getQuantity();
		}
		return i;
	}
	
	public int getNumberOfGpusNeeded(){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().isGraphicsCard()){
				i+=job.getQuantity();
			}
		}
		return i;
	}
	
	public int getNumberOfRamsNeeded(){
		int i=0;
		for(Job job:jobs){
			int rmc=job.getConfiguration().getRamModuleCount();
			i+=job.getQuantity()*rmc;
		}
		return i;
	}
	
	public int getNumberOfDualCore02sNeeded(){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().getCpuType().equals(CpuType.DUAL_CORE_2)){
				i+=job.getQuantity();
			}
		}
		return i;
	}
	
	public int getNumberOfDualCore24sNeeded(){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().getCpuType().equals(CpuType.DUAL_CORE_24)){
				i+=job.getQuantity();
			}
		}
		return i;
	}
	
	public int getNumberOfSingleCore16CPUsNeeded(){
		int i=0;
		for(Job job:jobs){
			if(job.getConfiguration().getCpuType().equals(CpuType.SINGLE_CORE_16)){
				i+=job.getQuantity();
			}
		}
		return i;
	}
}
