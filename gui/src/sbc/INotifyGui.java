package sbc;

import java.util.List;

import sbc.dto.StorageState;
import sbc.job.Job;

/**
 * Notify interface for notifying the gui about changes in the backend
 */
public interface INotifyGui {
	
	public void updateStorage(StorageState state);
	
	public void updateJobs(List<Job> jobs);
	
	public void addLogMessage(String message);

}
