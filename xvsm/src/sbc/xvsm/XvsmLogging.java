package sbc.xvsm;

public class XvsmLogging {
	private static XvsmLogging logging; 
	
	public synchronized static XvsmLogging getInstance(){
		if(logging == null){
			logging = new XvsmLogging();
		}
		return logging;
	}
	
	private XvsmLogging(){
		initXvsm();
	}
	
	private void initXvsm(){
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
