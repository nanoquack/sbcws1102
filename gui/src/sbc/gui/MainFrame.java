package sbc.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sbc.INotifyGui;
import sbc.dto.StorageState;

public class MainFrame extends JFrame implements INotifyGui {
	protected JPanel menuPanel;
	protected JPanel contentPanel;
	protected JEditorPane logPane;
	protected JComboBox implChooser;

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
		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		logPane = new JEditorPane();
		logPane.setBorder(BorderFactory.createTitledBorder(Constants.LABEL_LOG_PANE));
		logPane.setEditable(false);
		contentPanel.add(logPane, BorderLayout.CENTER);
		add(contentPanel, BorderLayout.CENTER);
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
