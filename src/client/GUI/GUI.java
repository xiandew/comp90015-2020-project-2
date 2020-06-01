package client.GUI;

import java.util.Enumeration;
import java.util.LinkedList;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
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
	public DefaultTableModel modelUsers;
	public JTable tableUsers;
	public DefaultTableModel modelNotifications;
	public JTable tableNotifications;

	private JPanel panelUsers;
	private JScrollPane scrollPaneUsers;
	private JScrollPane scrollPaneNotifications;
	private DefaultTableCellRenderer centerRenderer;

	public GUI() {
		centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		initialize();
	}

	public void launch() {
		this.frmMain.setVisible(true);
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

		scrollPaneNotifications = new JScrollPane();
		scrollPaneNotifications.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneNotifications.setBounds(14, 35, 282, 268);
		panelNotifications.add(scrollPaneNotifications);

		modelUsers = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Username", "Action" });
		tableUsers = new JTable(modelUsers);
		tableUsers.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		modelUsers.addTableModelListener(new ColumnWidthResizer(tableUsers, "Username", scrollPaneUsers.getWidth()));

		tableUsers.addMouseListener(new CursorDefaultListener(scrollPaneUsers));
		tableUsers.addMouseMotionListener(new CursorPointerListener(tableUsers, scrollPaneUsers, "Kick Out"));
		tableUsers.getColumn("ID").setCellRenderer(centerRenderer);
		tableUsers.getColumn("Action").setCellRenderer(centerRenderer);
		scrollPaneUsers.setViewportView(tableUsers);

		modelNotifications = new DefaultTableModel(new Object[][] {}, new String[] { "UserID", "Message", "Action" });
		tableNotifications = new JTable(modelNotifications);
		tableNotifications.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		modelNotifications.addTableModelListener(
				new ColumnWidthResizer(tableNotifications, "Message", scrollPaneNotifications.getWidth()));
		tableNotifications.addMouseListener(new CursorDefaultListener(scrollPaneNotifications));
		tableNotifications.addMouseMotionListener(
				new CursorPointerListener(tableNotifications, scrollPaneNotifications, "Approve"));
		tableNotifications.getColumn("UserID").setCellRenderer(centerRenderer);
		tableNotifications.getColumn("Action").setCellRenderer(centerRenderer);
		scrollPaneNotifications.setViewportView(tableNotifications);

		// Resize user list panels
		panelUsers.setBounds(647, 315, 310, 302);
		scrollPaneUsers.setBounds(14, 23, 282, 266);
	}

	public void updateUserList(LinkedList<JSONObject> users, boolean isManager) {
		modelUsers.setRowCount(0);
		if (isManager) {
			for (int i = 0; i < users.size(); i++) {
				JSONObject user = users.get(i);
				int id = Integer.parseInt((String) user.get("id"));
				if (Boolean.parseBoolean((String) user.get("isManager"))) {
					modelUsers.addRow(new Object[] { id, user.get("displayName"), "N/A" });
				} else {
					modelUsers.addRow(new Object[] { id, user.get("displayName"), "Kick Out" });
				}
			}
		} else {
			for (int i = 0; i < users.size(); i++) {
				JSONObject user = users.get(i);
				int id = Integer.parseInt((String) user.get("id"));
				modelUsers.addRow(new Object[] { id, user.get("displayName") });
			}
		}
	}

	public void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(frmMain, msg);
	}

	public int showConfirmDialog(String msg) {
		return JOptionPane.showConfirmDialog(frmMain, msg, "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
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

		modelUsers = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Username" });
		tableUsers = new JTable(modelUsers);
		tableUsers.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		modelUsers.addTableModelListener(new ColumnWidthResizer(tableUsers, "Username", scrollPaneUsers.getWidth()));
		tableUsers.getColumn("ID").setCellRenderer(centerRenderer);
		scrollPaneUsers.setViewportView(tableUsers);

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

	class ColumnWidthResizer implements TableModelListener {
		private JTable table;
		private String excludedColIdentifier;
		private int includedColWidth;
		private int parentWidth;
		private int spacing = 16;

		public ColumnWidthResizer(JTable table, String excludedColIdentifier, int parentWidth) {
			this.table = table;
			this.excludedColIdentifier = excludedColIdentifier;
			this.parentWidth = parentWidth;
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			includedColWidth = 0;
			for (int column = 0; column < table.getColumnCount(); column++) {
				TableColumn tableColumn = table.getColumnModel().getColumn(column);

				if (excludedColIdentifier.equals(tableColumn.getIdentifier())) {
					continue;
				}

				Object value = tableColumn.getHeaderValue();
				TableCellRenderer renderer = tableColumn.getHeaderRenderer();
				if (renderer == null) {
					renderer = table.getTableHeader().getDefaultRenderer();
				}
				int colHeaderWidth = renderer.getTableCellRendererComponent(table, value, false, false, -1, column)
						.getPreferredSize().width;

				int preferredWidth = Math.max(tableColumn.getMinWidth(), colHeaderWidth);
				int maxWidth = tableColumn.getMaxWidth();

				for (int row = 0; row < table.getRowCount(); row++) {
					TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
					Component c = table.prepareRenderer(cellRenderer, row, column);
					int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
					preferredWidth = Math.max(preferredWidth, width);

					// We've exceeded the maximum width, no need to check other rows
					if (preferredWidth >= maxWidth) {
						preferredWidth = maxWidth;
						break;
					}
				}

				int width = preferredWidth + spacing;
				tableColumn.setPreferredWidth(width);
				includedColWidth += width;
			}
			table.getColumn(excludedColIdentifier).setPreferredWidth(parentWidth - includedColWidth);
		}
	}

	class CursorPointerListener extends MouseMotionAdapter {
		private JTable table;
		private JScrollPane pane;
		private String targetAction;

		CursorPointerListener(JTable table, JScrollPane pane, String targetAction) {
			super();
			this.table = table;
			this.pane = pane;
			this.targetAction = targetAction;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int row = table.rowAtPoint(e.getPoint());
			int col = table.columnAtPoint(e.getPoint());
			if (row >= 0 && col == table.getColumn("Action").getModelIndex()
					&& table.getValueAt(row, col) == this.targetAction) {
				pane.setCursor(new Cursor(Cursor.HAND_CURSOR));
			} else {
				pane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	class CursorDefaultListener extends MouseAdapter {
		private JScrollPane pane;

		public CursorDefaultListener(JScrollPane pane) {
			super();
			this.pane = pane;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
