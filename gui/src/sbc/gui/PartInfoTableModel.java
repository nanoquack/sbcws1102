package sbc.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import sbc.dto.StorageState;

public class PartInfoTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -3006673063717839407L;
	protected StorageState state;
	protected List<String> partNames;

	/**
	 * Updates the storage state information of the table model to the give
	 * state.
	 * 
	 * @param state
	 *            the new state information
	 */
	public void updateState(StorageState state) {
		this.state = state;
		this.fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return 3;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return Constants.LABEL_PART_INFO_TABLE_PART;
		case 1:
			return Constants.LABEL_PART_INFO_TABLE_COUNT;
		default:
			return "";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getPartName(rowIndex);
		case 1:
			return getPartState(rowIndex);
		default:
			return "";
		}
	}

	protected String getPartName(int partIndex) {
		switch (partIndex) {
		case 0:
			return "cpu";
		case 1:
			return "ram";
		case 2:
			return "mainboard";
		case 3:
			return "gpu";
		default:
			return "";
		}
	}

	protected int getPartState(int partIndex) {
		switch (partIndex) {
		case 0:
			return state.getCpu();
		case 1:
			return state.getRam();
		case 2:
			return state.getMainboard();
		case 3:
			return state.getGpu();
		default:
			return 0;
		}
	}

}
