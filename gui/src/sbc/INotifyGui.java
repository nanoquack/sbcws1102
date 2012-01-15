package sbc;

import sbc.dto.StorageState;
import sbc.job.Job;

/**
 * Notify interface for notifying the gui about changes in the backend
 */
public interface INotifyGui {
	
	public void updateStorage(StorageState state);
	
	public void addLogMessage(String message);

	public void addJob(Job job);
	
	public void removeJob(Job job);
}
