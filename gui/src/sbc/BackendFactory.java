package sbc;

import sbc.gui.Constants;

public class BackendFactory {
	public static IBackend getBackend(String implType){
//		if(implType.equals(Constants.LABEL_JMS_IMPL)){
//			sbc.jms.Backend backend = new sbc.jms.Backend();
//			return backend;
//		}
//		if(implType.equals(Constants.LABEL_JMS_IMPL)){
//			sbc.xvsm.Backend backend = new sbc.xvsm.Backend();
//			return backend;
//		}
		throw new RuntimeException("Implementation " + implType + "is not known");
	}
}
