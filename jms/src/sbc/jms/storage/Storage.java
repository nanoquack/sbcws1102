package sbc.jms.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import sbc.dto.ComponentEnum;
import sbc.dto.CpuComponent;
import sbc.dto.GpuComponent;
import sbc.dto.MainboardComponent;
import sbc.dto.ProductComponent;
import sbc.dto.RamComponent;
import sbc.dto.StorageState;
import sbc.dto.CpuComponent.CpuType;
import sbc.jms.JmsLogging;

/**
 * Container class for the components stored
 * Offers different methods to obtain items
 */
public class Storage {

	private HashMap<String,LinkedList<ProductComponent>> storage=new HashMap<String,LinkedList<ProductComponent>>();

	public synchronized void storeItem(String ce,ProductComponent pc){
		LinkedList<ProductComponent> list=storage.get(ce);
		if(list==null){
			list=new LinkedList<ProductComponent>();
			storage.put(ce, list);
		}
		list.addLast(pc);
	}

	/**
	 * Check if all necessary items for a PC are in the storage
	 * @return PC items if storing, else null
	 */
	public ArrayList<ProductComponent> getPcItemsIfAvailable(){
		ArrayList<ProductComponent> allPcUnits=new ArrayList<ProductComponent>();

		//Check, if CPU, RAM and Mainboard are available
		LinkedList<ProductComponent> cpuList=storage.get(CpuComponent.class.getName());
		if(cpuList==null || cpuList.isEmpty()){return null;}
		LinkedList<ProductComponent> mainboardList=storage.get(MainboardComponent.class.getName());
		if(mainboardList==null || mainboardList.isEmpty()){return null;}
		LinkedList<ProductComponent> ramList=storage.get(RamComponent.class.getName());
		if(ramList==null || ramList.isEmpty()){return null;}

		//add CPU, Mainboard and RAM to List and remove them from the storage
		allPcUnits.add(cpuList.pollFirst());
		allPcUnits.add(mainboardList.pollFirst());
		if(!ramList.isEmpty()){
			allPcUnits.add(ramList.pollFirst());
		}if(!ramList.isEmpty()){	//if possible add second ram
			allPcUnits.add(ramList.pollFirst());
		}if(ramList.size()>=2){
			allPcUnits.add(ramList.pollFirst());
			allPcUnits.add(ramList.pollFirst());
		}

		//if GPU is available: add it to list and remove it from storage
		LinkedList<ProductComponent> gpuList=storage.get(GpuComponent.class.getName());
		if(gpuList!=null && !gpuList.isEmpty()){
			allPcUnits.add(gpuList.pollFirst());
		}

		JmsLogging.getInstance().log("Produced Pc, storage now:" +
				"cpu "+cpuList.size()+", ram "+ramList.size()+" "
				+"mainboard "+mainboardList.size());

		return allPcUnits;

	}

	/**
	 * Get the status of the storage for updating the GUI
	 */
	public StorageState getStorageState(){
		StorageState state=new StorageState();

		LinkedList<ProductComponent> cpuList=storage.get(CpuComponent.class.getName());
		if(cpuList==null || cpuList.isEmpty()){state.setCpu(0);}
		else{state.setCpu(cpuList.size());}
		LinkedList<ProductComponent> mainboardList=storage.get(MainboardComponent.class.getName());
		if(mainboardList==null || mainboardList.isEmpty()){state.setMainboard(0);}
		else{state.setMainboard(mainboardList.size());}
		LinkedList<ProductComponent> ramList=storage.get(RamComponent.class.getName());
		if(ramList==null || ramList.isEmpty()){state.setRam(0);}
		else{state.setRam(ramList.size());}
		LinkedList<ProductComponent> gpuList=storage.get(GpuComponent.class.getName());
		if(gpuList==null || gpuList.isEmpty()){state.setGpu(0);}
		else{state.setGpu(gpuList.size());}

		return state;
	}

	public int getNumberOfRams(){
		try{
			return storage.get(RamComponent.class.getName()).size();
		}catch(NullPointerException ex){
			return 0;
		}
	}

	public int getNumberOfMainboards(){
		try{
			return storage.get(MainboardComponent.class.getName()).size();
		}catch(NullPointerException ex){
			return 0;
		}
	}

	public int getNumberOfGpus(){
		try{
			return storage.get(GpuComponent.class.getName()).size();
		}catch(NullPointerException ex){
			return 0;
		}
	}

	public int getNumberOfSingleCore16CPU(){
		try{
			int i=0;
			LinkedList<ProductComponent> cpuComponents=storage.get(CpuComponent.class.getName());
			for(ProductComponent comp:cpuComponents){
				CpuComponent cpu=(CpuComponent)comp;
				if(cpu.getCpuType().equals(CpuType.SINGLE_CORE_16)){
					i++;
				}
			}
			return i;
		}catch(NullPointerException ex){
			return 0;
		}
	}
	public int getNumberOfDualCore02CPU(){
		try{
			int i=0;
			LinkedList<ProductComponent> cpuComponents=storage.get(CpuComponent.class.getName());
			for(ProductComponent comp:cpuComponents){
				CpuComponent cpu=(CpuComponent)comp;
				if(cpu.getCpuType().equals(CpuType.DUAL_CORE_2)){
					i++;
				}
			}
			return i;
		}catch(NullPointerException ex){
			return 0;
		}
	}

	public int getNumberOfDualCore24CPU(){
		try{
			int i=0;
			LinkedList<ProductComponent> cpuComponents=storage.get(CpuComponent.class.getName());
			for(ProductComponent comp:cpuComponents){
				CpuComponent cpu=(CpuComponent)comp;
				if(cpu.getCpuType().equals(CpuType.DUAL_CORE_24)){
					i++;
				}
			}
			return i;
		}catch(NullPointerException ex){
			return 0;
		}
	}

	public ProductComponent removeComponent(ProductComponent comp){
		try{
			if(comp instanceof RamComponent){
				return storage.get(RamComponent.class.getName()).pollFirst();
			}if(comp instanceof CpuComponent){	//TODO: mehrere typen von cpu
				return storage.get(CpuComponent.class.getName()).pollFirst();
			}if(comp instanceof GpuComponent){
				return storage.get(GpuComponent.class.getName()).pollFirst();
			}if(comp instanceof MainboardComponent){
				return storage.get(MainboardComponent.class.getName()).pollFirst();
			}
			return null;
		}catch(NullPointerException ex){
			ex.printStackTrace();
			return null;
		}
	}

}
