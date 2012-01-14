package sbc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
import sbc.dto.CpuComponent;
import sbc.dto.ProductionOrder;
import sbc.dto.StorageState;

public class MainFrame extends JFrame implements INotifyGui, ItemListener,
		ActionListener {
	protected IBackend backend;
	protected JPanel menuPanel;
	protected JPanel configPanel;
	protected JPanel contentPanel;
	protected JPanel managementPanel;
	protected JPanel jobPanel;
	protected JPanel partInfoPanel;
	protected JTable partInfoTable;
	protected JSplitPane infoSplitPane;
	protected JTextPane logPane;
	protected DefaultStyledDocument logText;
	protected JComboBox implChooser;
	protected JButton clearLogBtn;
	protected JButton createProducerButton;
	protected JComboBox producerProductType;
	protected JTextField producerProductCount;
	protected JTextField producerErrorRate;
	protected String factoryInfo;
	protected JComboBox jobCpuType;
	protected JComboBox jobRamCount;
	protected JCheckBox jobGraphicsCard;

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
		implChooser.addItem(Constants.LABEL_NULL_IMPL);
		implChooser.addItem(Constants.LABEL_JMS_IMPL);
		implChooser.addItem(Constants.LABEL_XVSM_IMPL);
		implChooser.addItemListener(this);
		clearLogBtn = new JButton(Constants.LABEL_CLEAR_LOG_BUTTON);
		clearLogBtn.addActionListener(this);
		menuPanel.add(implChooserLabel);
		menuPanel.add(implChooser);
		menuPanel.add(clearLogBtn);
		add(menuPanel, BorderLayout.NORTH);
	}

	protected void initContentPanel() {
		JScrollPane scrollPane = new JScrollPane();
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		initManagementPanel();
		initPartInfoPanel();
		initLogPanel();
		initJobPanel();
		configPanel = new JPanel();
		contentPanel.add(managementPanel);
		contentPanel.add(jobPanel);
		contentPanel.add(configPanel);
		contentPanel.add(infoSplitPane);
		scrollPane.setViewportView(contentPanel);
		add(scrollPane, BorderLayout.CENTER);
		initJobPanel();
	}
	
	protected void initManagementPanel(){
		managementPanel = new JPanel();
		managementPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		managementPanel.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_PRODUCTION_PANEL));
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
	}
	
	protected void initJobPanel(){
		jobPanel = new JPanel();
		jobPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		jobPanel.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_JOB_PANEL));
		JLabel cpuTypeLabel = new JLabel(Constants.LABEL_JOB_CPU_TYPE);
		JLabel ramCountLabel = new JLabel(Constants.LABEL_JOB_RAM_MODULE_COUNT);
		JLabel graphicsCardLabel = new JLabel(Constants.LABEL_JOB_GRAPHICS_CARD);

		jobCpuType = new JComboBox();
		jobRamCount = new JComboBox();
		jobGraphicsCard = new JCheckBox();
		jobCpuType.setPreferredSize(new Dimension(Constants.JOB_CPU_TYPE_WIDTH, Constants.JOB_CPU_TYPE_HEIGHT));
		jobRamCount.setPreferredSize(new Dimension(Constants.JOB_RAM_COUNT_WIDTH, Constants.JOB_RAM_COUNT_HEIGHT));
		jobGraphicsCard.setPreferredSize(new Dimension(Constants.JOB_GRAPHICS_CARD_WIDTH, Constants.JOB_GRAPHICS_CARD_HEIGHT));
		fillJobCpuType();
		fillJobRamCount();
		
		jobPanel.add(cpuTypeLabel);
		jobPanel.add(jobCpuType);
		jobPanel.add(ramCountLabel);
		jobPanel.add(jobRamCount);
		jobPanel.add(graphicsCardLabel);
		jobPanel.add(jobGraphicsCard);
	}
	
	private void fillJobCpuType(){
		for (CpuComponent.CpuType val : CpuComponent.CpuType.values()) {
			jobCpuType.addItem(val.toString());
		}
	}
	
	private void fillJobRamCount(){
		jobRamCount.addItem(Constants.LABEL_JOB_RAM_MODULE_COUNT_1);
		jobRamCount.addItem(Constants.LABEL_JOB_RAM_MODULE_COUNT_2);
		jobRamCount.addItem(Constants.LABEL_JOB_RAM_MODULE_COUNT_4);
	}
	
	protected void initPartInfoPanel(){
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
	}
	
	protected void initLogPanel(){
		StyleContext sc = new StyleContext();
		logText = new DefaultStyledDocument(sc);
		logPane = new JTextPane(logText);
		logPane.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_LOG_PANE));
		logPane.setPreferredSize(new Dimension(200, 200));
		logPane.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logPane);
		logScrollPane.setBorder(BorderFactory
				.createTitledBorder(Constants.LABEL_LOG_PANE));
		logScrollPane.setPreferredSize(new Dimension(200, 200));
		logScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		logScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logScrollPane.setPreferredSize(new Dimension(250, 250));
		logScrollPane.setMinimumSize(new Dimension(10, 10));
		infoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, partInfoPanel, logScrollPane);
		infoSplitPane.setMinimumSize(new Dimension(800, 400));
		infoSplitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
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
			logText.insertString(logText.getLength(), message + "\n", null);
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
				backend.startSystem(this, factoryInfo);
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
		if(evt.getSource()== clearLogBtn){
			logPane.setStyledDocument(new DefaultStyledDocument());
		}
	}

	private void createNewProducer(ComponentEnum productType, int productCount,
			int errorRate) {
		List<ProductionOrder> orderList = new ArrayList<ProductionOrder>();
		ProductionOrder order1 = new ProductionOrder(productType, productCount);
		orderList.add(order1);
		backend.createProducer(orderList, errorRate);
	}

	public String getFactoryInfo() {
		return factoryInfo;
	}

	public void setFactoryInfo(String factoryInfo) {
		this.factoryInfo = factoryInfo;
	}

	
}
