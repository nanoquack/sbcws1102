package sbc;

import sbc.jms.Backend;

public class Main {

	public static void main(String args[]){

		try {
			//Backend starten
			Backend backend=new Backend();
			//GUI starten
			backend.initializeFactory();
			backend.createProducer(2, 10);
			Thread.sleep(2000);

			backend.createProducer(2, 30);
			Thread.sleep(2000);

			backend.shutDownFactory();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
