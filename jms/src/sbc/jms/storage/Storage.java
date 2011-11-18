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
		
		System.out.println("Produced Pc, storage now:" +
				"cpu "+cpuList.size()+", ram "+ramList.size()+" "
				+"mainboard "+mainboardList.size());
		
		return allPcUnits;
		
	}

	
	
//	/**
//	 * 
//	 * @return Specified element or null, if storage is empty
//	 */
//	public synchronized ProductComponent getItem(ComponentEnum ce){
//		LinkedList<ProductComponent> list=storage.get(ce.ordinal());
//		return list.pollFirst();
//	}
//
//	/**
//	 * Tries to get an item of the specified type within the specified time
//	 * @return Specified element or null, if time runs out and storage is still empty
//	 */
//	public synchronized ProductComponent getItem(ComponentEnum ce, long blockingTime) throws InterruptedException{
//		LinkedList<ProductComponent> list=storage.get(ce.ordinal());
//		if(!list.isEmpty()){ return list.pollFirst();}
//		else{
//			while(blockingTime>0){
//				if(blockingTime>100){
//					Thread.sleep(100);
//					if(!list.isEmpty()){ return list.pollFirst();}
//					blockingTime=blockingTime-100;
//				}else{
//					Thread.sleep(blockingTime);
//					return list.pollFirst();
//				}
//			}
//		}
//		return null; //unreachable (only for compiler
//	}
//	
//	/**
//	 * Special method for obtaining RAM modules.
//	 * @return List with 1,2 or 4 RAM modules, depending on the current storage (the maximum is chosen) 
//	 */
//	public synchronized List<ProductionComponent> getRamModules(){
//		LinkedList<ProductComponent> list=storage.get(ComponentEnum.RAM);
//		if(list.size()>=4){
//	}

}
