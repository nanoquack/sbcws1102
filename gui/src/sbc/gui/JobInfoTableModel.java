package sbc.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import sbc.dto.StorageState;
import sbc.job.Job;

public class JobInfoTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -7201123564499432628L;
	protected List<Job> jobs;
	protected List<String> partNames;

	public JobInfoTableModel(){
		jobs = new ArrayList<Job>();
	}
	
	public synchronized void updateJobs(List<Job> update){
		jobs = update;
		this.fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return jobs.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return Constants.LABEL_JOB_INFO_TABLE_UUID;
		case 1:
			return Constants.LABEL_JOB_INFO_TABLE_QUANTITY;
		case 2:
			return Constants.LABEL_JOB_INFO_TABLE_CONFIGURATION;
		default:
			return "";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return jobs.get(rowIndex).getUuid();
		case 1:
			return jobs.get(rowIndex).getQuantity();
		case 2: 
			return jobs.get(rowIndex).getConfiguration().toString();
		default:
			return "";
		}
	}
}
