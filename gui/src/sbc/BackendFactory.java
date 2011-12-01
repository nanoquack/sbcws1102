package sbc;

import sbc.gui.Constants;

public class BackendFactory {
	public static IBackend getBackend(String implType){
		if(implType.equals(Constants.LABEL_JMS_IMPL)){
				try {
					Class clz = Class.forName("sbc.jms.Backend");
					Object instance = clz.newInstance();
					if(instance instanceof IBackend){
						return (IBackend)instance;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		if(implType.equals(Constants.LABEL_XVSM_IMPL)){
			try {
				Class clz = Class.forName("sbc.xvsm.Backend");
				Object instance = clz.newInstance();
				if(instance instanceof IBackend){
					return (IBackend)instance;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		throw new RuntimeException("Implementation " + implType + "is not known");
	}
}
