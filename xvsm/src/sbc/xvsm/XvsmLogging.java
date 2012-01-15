package sbc.xvsm;

import java.net.URI;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;

import sbc.SbcConstants;

public class XvsmLogging {
	private static XvsmLogging logging;
	private DefaultMzsCore core;
	private Capi capi;
	private ContainerReference notificationContainer;
	
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
		try{
			core = DefaultMzsCore.newInstance(0);
			capi = new Capi(core);
			notificationContainer = capi.lookupContainer(
					SbcConstants.NOTIFICATIONCONTAINER, new URI(
							"xvsm://localhost:+"+(SbcConstants.MAINPORT+SbcConstants.LOGGERPORTOFFSET)),
					MzsConstants.RequestTimeout.INFINITE, null);
		}
		catch(Exception e){
			System.err.println("XvsmLogging: Could not find notification container");
		}
	}
	
	public void log(String msg){
		try{
			capi.write(notificationContainer, new Entry(msg));
		}
		catch(Exception e){
			System.err.println("XvsmLogging: Could not write log message to notification container");
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
