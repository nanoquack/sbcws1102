package sbc;

import sbc.dto.StorageState;

public interface INotifyGui {
	
	public void updateStorage(StorageState state);
	
	public void addLogMessage(String message);

}
