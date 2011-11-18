package sbc;

import java.util.ArrayList;
import java.util.List;

import sbc.dto.ComponentEnum;
import sbc.dto.ProductComponent;
import sbc.dto.ProductionOrder;
import sbc.jms.Backend;

public class Main {

	public static void main(String args[]){

		try {
			//Backend starten
			Backend backend=new Backend();
			//GUI starten
			backend.initializeFactory();
			
			List<ProductionOrder> productionList=new ArrayList<ProductionOrder>();
			productionList.add(new ProductionOrder(ComponentEnum.CPU,2));
			productionList.add(new ProductionOrder(ComponentEnum.MAINBOARD,2));
			productionList.add(new ProductionOrder(ComponentEnum.RAM,6));
			productionList.add(new ProductionOrder(ComponentEnum.GPU,1));
			backend.createProducer(productionList,40);
			Thread.sleep(10);

			List<ProductionOrder> productionList2=new ArrayList<ProductionOrder>();
			productionList2.add(new ProductionOrder(ComponentEnum.CPU,1));
			productionList2.add(new ProductionOrder(ComponentEnum.MAINBOARD,1));
			productionList2.add(new ProductionOrder(ComponentEnum.RAM,2));
			productionList2.add(new ProductionOrder(ComponentEnum.GPU,1));
			
			backend.createProducer(productionList2,30);
			Thread.sleep(10000);

			backend.shutDownFactory();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
