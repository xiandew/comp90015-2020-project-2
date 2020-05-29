package client.GUI;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.json.JSONObject;

import utils.ActionType;

public class GUI {

	public Board board;
	public JMenuItem mntmNew = new JMenuItem("New");
	public JMenuItem mntmOpen = new JMenuItem("Open");
	public JMenuItem mntmSave = new JMenuItem("Save");
	public JMenuItem mntmSaveAs = new JMenuItem("Save As");
	public JFileChooser fileChooser = new JFileChooser();
	public JFrame frmMain;

	private JPanel panelUsers;
	private JScrollPane scrollPaneUsers;
	private JTextPane textPaneUsers;
	private HTMLEditorKit textPaneUsersKit;
	private HTMLDocument textPaneUsersDoc;
	private JTextPane textPaneNotifications;
	private HTMLEditorKit textPaneNotificationsKit;
	private HTMLDocument textPaneNotificationsDoc;

	public GUI() {
		initialize();
	}

	public void launch() {
		this.frmMain.setVisible(true);
	}

	public void launchAsManager() {
		this.addManagerControls();
		this.launch();
	}

	public void addManagerControls() {
		frmMain.setBounds(100, 100, 975, 691);

		JMenuBar menuBar = new JMenuBar();
		frmMain.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mnFile.add(mntmNew);
		mnFile.add(mntmOpen);
		mnFile.add(mntmSave);
		mnFile.add(mntmSaveAs);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener((ActionEvent e) -> {
			System.exit(0);
		});
		mnFile.add(mntmExit);

		// Notification panels
		JPanel panelNotifications = new JPanel();
		panelNotifications.setBounds(647, 0, 310, 316);
		frmMain.getContentPane().add(panelNotifications);
		panelNotifications.setLayout(null);

		JLabel lblNotifications = new JLabel("Notifications");
		lblNotifications.setBounds(14, 13, 104, 18);
		panelNotifications.add(lblNotifications);

		JScrollPane scrollPaneNotifications = new JScrollPane();
		scrollPaneNotifications.setBounds(14, 35, 282, 268);
		panelNotifications.add(scrollPaneNotifications);

		// Resize user list panels
		panelUsers.setBounds(647, 315, 310, 302);
		scrollPaneUsers.setBounds(14, 23, 282, 266);
	}

	public void updateUserList(LinkedList<JSONObject> users) {
		try {
			textPaneUsers.setText("");
			for (JSONObject user : users) {
				textPaneUsersKit.insertHTML(textPaneUsersDoc, textPaneUsersDoc.getLength(),
						String.format("<div>%s</div>", user.get("displayName")), 0, 0, null);
			}
		} catch (BadLocationException | IOException e1) {
			System.out.println("Error when updating html");
		}
	}

	public static void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMain = new JFrame();
		frmMain.setTitle("Distributed Shared White Board - COMP90015 Assignment 2, fall 2020");
		frmMain.setBounds(100, 100, 975, 664);
		frmMain.setResizable(false);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMain.getContentPane().setLayout(null);

		board = new Board();
		board.setBorder(new LineBorder(new Color(0, 0, 0)));
		board.setBounds(14, 13, 619, 551);
		board.initCanvas();
		frmMain.getContentPane().add(board);
		board.setLayout(null);

		panelUsers = new JPanel();
		panelUsers.setBounds(647, 13, 310, 604);
		frmMain.getContentPane().add(panelUsers);
		panelUsers.setLayout(null);

		JLabel lblUsers = new JLabel("Users");
		lblUsers.setBounds(14, 0, 72, 18);
		panelUsers.add(lblUsers);

		scrollPaneUsers = new JScrollPane();
		scrollPaneUsers.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneUsers.setBounds(14, 23, 282, 568);
		panelUsers.add(scrollPaneUsers);

		textPaneUsers = new JTextPane();
		textPaneUsers.setEditable(false);
		textPaneUsers.setContentType("text/html");
		textPaneUsersKit = new HTMLEditorKit();
		textPaneUsersDoc = new HTMLDocument();
		textPaneUsers.setEditorKit(textPaneUsersKit);
		textPaneUsers.setDocument(textPaneUsersDoc);
		scrollPaneUsers.setViewportView(textPaneUsers);

		ButtonGroup buttonGroupBoardActions = new ButtonGroup();

		JRadioButton rdbtnLine = new JRadioButton(ActionType.LINE.toString());
		rdbtnLine.setSelected(true);
		rdbtnLine.setBounds(14, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnLine);
		buttonGroupBoardActions.add(rdbtnLine);

		JRadioButton rdbtnCircle = new JRadioButton(ActionType.CIRCLE.toString());
		rdbtnCircle.setBounds(81, 577, 77, 27);
		frmMain.getContentPane().add(rdbtnCircle);
		buttonGroupBoardActions.add(rdbtnCircle);

		JRadioButton rdbtnRect = new JRadioButton(ActionType.RECT.toString());
		rdbtnRect.setBounds(164, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnRect);
		buttonGroupBoardActions.add(rdbtnRect);

		JRadioButton rdbtnText = new JRadioButton(ActionType.TEXT.toString());
		rdbtnText.setBounds(231, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnText);
		buttonGroupBoardActions.add(rdbtnText);

		JRadioButton rdbtnEraser = new JRadioButton(ActionType.ERASER.toString());
		rdbtnEraser.setBounds(298, 577, 77, 27);
		frmMain.getContentPane().add(rdbtnEraser);
		buttonGroupBoardActions.add(rdbtnEraser);

		ActionListener selectBoardAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				board.selectedAction = ActionType.valueOf(((JRadioButton) e.getSource()).getText());
			}
		};

		// Bind button listener
		Enumeration<AbstractButton> elements = buttonGroupBoardActions.getElements();
		while (elements.hasMoreElements()) {
			AbstractButton button = (AbstractButton) elements.nextElement();
			button.addActionListener(selectBoardAction);
		}
	}
}
