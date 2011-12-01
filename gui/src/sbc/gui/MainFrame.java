package sbc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;

import sbc.BackendFactory;
import sbc.IBackend;
import sbc.INotifyGui;
import sbc.dto.ComponentEnum;
import sbc.dto.ProductionOrder;
import sbc.dto.StorageState;

public class MainFrame extends JFrame implements INotifyGui, ItemListener,
		ActionListener {
	protected IBackend backend;
	protected JPanel menuPanel;
	protected JPanel configPanel;
	protected JPanel contentPanel;
	protected JPanel managementPanel;
	protected JPanel partInfoPanel;
	protected JTable partInfoTable;
	protected JTextPane logPane;
	protected DefaultStyledDocument logText;
	protected JComboBox implChooser;
	protected JButton createProducerButton;
	protected JComboBox producerProductType;
	protected JTextField producerProductCount;
	protected JTextField producerErrorRate;

	public MainFrame() {
		initMainFrame();
		initComponents();
	}

	protected void initMainFrame() {
		setSize(Constants.MAIN_FRAME_WIDTH, Constants.MAIN_FRAME_HEIGHT);
		setLayout(new BorderLayout());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				MainFrame.this.shutdown();
			}
		});
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
		implChooser.addItemListener(this);
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
		managementPanel.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_MANAGEMENT_PANEL));
		JLabel producerProductTypeLabel = new JLabel(
				Constants.LABEL_PRODUCER_PRODUCT_TYPE);
		producerProductType = new JComboBox();
		fillProducerProductType();
		JLabel producerProductCountLabel = new JLabel(
				Constants.LABEL_PRODUCER_PRODUCT_COUNT);
		producerProductCount = new JTextField();
		producerProductCount.setPreferredSize(new Dimension(
				Constants.PRODUCER_PRODUCT_COUNT_WIDTH,
				Constants.PRODUCER_PRODUCT_COUNT_HEIGHT));
		JLabel producerErrorRateLabel = new JLabel(
				Constants.LABEL_PRODUCER_ERROR_RATE);
		producerErrorRate = new JTextField();
		producerErrorRate.setPreferredSize(new Dimension(
				Constants.PRODUCER_ERROR_RATE_WIDTH,
				Constants.PRODUCER_ERROR_RATE_HEIGHT));
		createProducerButton = new JButton(
				Constants.LABEL_CREATE_PRODUCER_BUTTON);
		createProducerButton.addActionListener(this);
		managementPanel.add(producerProductTypeLabel);
		managementPanel.add(producerProductType);
		managementPanel.add(producerProductCountLabel);
		managementPanel.add(producerProductCount);
		managementPanel.add(producerErrorRateLabel);
		managementPanel.add(producerErrorRate);
		managementPanel.add(createProducerButton);
		partInfoPanel = new JPanel();
		partInfoPanel.setLayout(new BorderLayout());
		partInfoPanel.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_PART_INFO_TABLE));
		partInfoTable = new JTable();
		partInfoTable.setModel(new PartInfoTableModel());
		JScrollPane partInfoTableScrollPane = new JScrollPane(partInfoTable);
		partInfoTableScrollPane.setPreferredSize(new Dimension(
				Constants.PART_INFO_TABLE_WIDTH,
				Constants.PART_INFO_TABLE_HEIGHT));
		partInfoTable.setFillsViewportHeight(true);
		partInfoPanel.add(partInfoTableScrollPane, BorderLayout.CENTER);
		StyleContext sc = new StyleContext();
		logText = new DefaultStyledDocument(sc);
		logPane = new JTextPane(logText);
		logPane.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_LOG_PANE));
		logPane.setEditable(false);
		configPanel = new JPanel();
		contentPanel.add(managementPanel);
		contentPanel.add(configPanel);
		contentPanel.add(partInfoPanel);
		contentPanel.add(logPane);
		scrollPane.setViewportView(contentPanel);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void fillProducerProductType() {
		for (ComponentEnum val : ComponentEnum.values()) {
			producerProductType.addItem(val.toString());
		}
	}

	@Override
	public void updateStorage(StorageState state) {
		// TODO Auto-generated method stub
		PartInfoTableModel model = (PartInfoTableModel) partInfoTable
				.getModel();
		model.updateState(state);
	}

	@Override
	public void addLogMessage(String message) {
		try {
			logText.insertString(0, message + "\n", null);
		} catch (BadLocationException e) {
			System.err.println("Could not write into log panel!");
		}
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			if (backend != null) {
				backend.shutdownSystem();
			}
			try {
				backend = BackendFactory.getBackend(evt.getItem().toString());
				backend.startSystem(this);
			} catch (RuntimeException e) {
				System.err
						.println("BackendFactory does not know backend implementation '"
								+ evt.getItem().toString() + "'");
			}
		}
	}

	public IBackend getBackend() {
		return backend;
	}

	public void setBackend(IBackend backend) {
		this.backend = backend;
	}

	public void shutdown() {
		if (backend != null) {
			backend.shutdownSystem();
		}
		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == createProducerButton) {
			ComponentEnum productType = ComponentEnum.valueOf(producerProductType.getSelectedItem().toString());
			int productCount = Integer.parseInt(producerProductCount.getText());
			int errorRate = Integer.parseInt(producerErrorRate.getText());
			createNewProducer(productType, productCount, errorRate);
		}
	}

	private void createNewProducer(ComponentEnum productType, int productCount,
			int errorRate) {
		List<ProductionOrder> orderList = new ArrayList<ProductionOrder>();
		ProductionOrder order1 = new ProductionOrder(productType, productCount);
		orderList.add(order1);
		backend.createProducer(orderList, errorRate);
	}

}
