package sbc.gui;

import javax.swing.table.AbstractTableModel;

import sbc.dto.StorageState;

public class PartInfoTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -3006673063717839407L;
	protected StorageState state;

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
		// TODO Auto-generated method stub
		return null;
	}

}
