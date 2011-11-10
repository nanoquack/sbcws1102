package sbc;

import sbc.dto.StorageState;

/**
 * Notify interface for notifying the gui about changes in the backend
 */
public interface INotifyGui {
	
	public void updateStorage(StorageState state);
	
	public void addLogMessage(String message);

}
