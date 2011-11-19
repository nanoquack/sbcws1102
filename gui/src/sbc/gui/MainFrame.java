package sbc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import sbc.INotifyGui;
import sbc.dto.StorageState;

public class MainFrame extends JFrame implements INotifyGui {
	protected JPanel menuPanel;
	protected JPanel contentPanel;
	protected JPanel managementPanel;
	protected JPanel partInfoPanel;
	protected JTable partInfoTable;
	protected JEditorPane logPane;
	protected JComboBox implChooser;
	protected JButton createProducerButton;
	protected JTextField producerProductCount;
	protected JTextField producerErrorRate;

	public MainFrame() {
		initMainFrame();
		initComponents();
	}

	protected void initMainFrame() {
		setSize(Constants.MAIN_FRAME_WIDTH, Constants.MAIN_FRAME_HEIGHT);
		setLayout(new BorderLayout());
	}

	protected void initComponents() {
		initMenuPanel();
		initContentPanel();
	}

	protected void initMenuPanel() {
		menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel implChooserLabel = new JLabel(Constants.LABEL_IMPL_CHOOSER);
		implChooser = new JComboBox();
		implChooser.addItem(Constants.LABEL_JMS_IMPL);
		implChooser.addItem(Constants.LABEL_XVSM_IMPL);
		menuPanel.add(implChooserLabel);
		menuPanel.add(implChooser);
		add(menuPanel, BorderLayout.NORTH);
	}

	protected void initContentPanel() {
		JScrollPane scrollPane = new JScrollPane();
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		managementPanel = new JPanel();
		managementPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		managementPanel.setBorder(BorderFactory.createTitledBorder(Constants.LABEL_MANAGEMENT_PANEL));
		JLabel producerProductCountLabel = new JLabel(Constants.LABEL_PRODUCER_PRODUCT_COUNT);
		producerProductCount = new JTextField();
		producerProductCount.setPreferredSize(new Dimension(Constants.PRODUCER_PRODUCT_COUNT_WIDTH, Constants.PRODUCER_PRODUCT_COUNT_HEIGHT));
		JLabel producerErrorRateLabel = new JLabel(Constants.LABEL_PRODUCER_ERROR_RATE);
		producerErrorRate = new JTextField();
		producerErrorRate.setPreferredSize(new Dimension(Constants.PRODUCER_ERROR_RATE_WIDTH, Constants.PRODUCER_ERROR_RATE_HEIGHT));
		createProducerButton = new JButton(Constants.LABEL_CREATE_PRODUCER_BUTTON);
		managementPanel.add(producerProductCountLabel);
		managementPanel.add(producerProductCount);
		managementPanel.add(producerErrorRateLabel);
		managementPanel.add(producerErrorRate);
		managementPanel.add(createProducerButton);
		partInfoPanel = new JPanel();
		partInfoPanel.setLayout(new BorderLayout());
		partInfoPanel.setBorder(BorderFactory.createTitledBorder(Constants.LABEL_PART_INFO_TABLE));
		partInfoTable = new JTable();
		partInfoTable.setModel(new PartInfoTableModel());
		JScrollPane partInfoTableScrollPane = new JScrollPane(partInfoTable);
		partInfoTableScrollPane.setPreferredSize(new Dimension(Constants.PART_INFO_TABLE_WIDTH, Constants.PART_INFO_TABLE_HEIGHT));
		partInfoTable.setFillsViewportHeight(true);
		partInfoPanel.add(partInfoTableScrollPane, BorderLayout.CENTER);
		logPane = new JEditorPane();
		logPane.setBorder(BorderFactory.createTitledBorder(Constants.LABEL_LOG_PANE));
		logPane.setEditable(false);
		contentPanel.add(managementPanel);
		contentPanel.add(partInfoPanel);
		contentPanel.add(logPane);
		scrollPane.setViewportView(contentPanel);
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void updateStorage(StorageState state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLogMessage(String message) {
		// TODO Auto-generated method stub

	}

}
