package sbc.mocking;

import sbc.INotifyGui;
import sbc.dto.StorageState;

public class NotifyGuiMock implements INotifyGui {

	@Override
	public void updateStorage(StorageState state) {
		System.out.println("State update erhalten, Anzahl" 
				+ " CPU: " + state.getCpu()
				+ " GPU: " + state.getGpu()
				+ " MB: " + state.getMainboard() 
				+ " RAM: " + state.getRam());
	}

	@Override
	public void addLogMessage(String message) {
		System.out.println("GUI-Log: "+message);

	}

}
