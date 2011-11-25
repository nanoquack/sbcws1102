package sbc.mocking;

import sbc.INotifyGui;
import sbc.dto.StorageState;

public class NotifyGuiMock implements INotifyGui {

	@Override
	public void updateStorage(StorageState state) {
		System.out.println("State update erhalten, CPU-Anzahl "+state.getCpu());

	}

	@Override
	public void addLogMessage(String message) {
		System.out.println("GUI-Log: "+message);

	}

}
