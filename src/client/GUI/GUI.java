package client.GUI;

import java.util.Enumeration;
import java.util.LinkedList;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
	public JTable tableUsers;
	private JPanel panelUsers;
	private JScrollPane scrollPaneUsers;

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
	
	public void updateNotificationLisst(LinkedList<JSONObject> notifications) {
		
	}

	public void updateUserList(LinkedList<JSONObject> users, boolean isManager) {
		String[] columnNames = null;
		Object[][] data = new Object[users.size()][];

		if (isManager) {
			columnNames = new String[] { "ID", "Username", "Action" };
			for (int i = 0; i < users.size(); i++) {
				JSONObject user = users.get(i);
				if (Boolean.parseBoolean((String) user.get("isManager"))) {
					data[i] = new Object[] { Integer.parseInt((String) user.get("id")), user.get("displayName"),
							"N/A" };
				} else {
					data[i] = new Object[] { Integer.parseInt((String) user.get("id")), user.get("displayName"),
							"Kick Out" };
				}
			}
		} else {
			columnNames = new String[] { "ID", "Username" };
			for (int i = 0; i < users.size(); i++) {
				JSONObject user = users.get(i);
				data[i] = new Object[] { Integer.parseInt((String) user.get("id")), user.get("displayName") };
			}
		}

		tableUsers = new JTable(new DefaultTableModel(data, columnNames));
		tableUsers.getColumn("Username").setCellRenderer(new MultilineTableCell());

		if (isManager) {
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			tableUsers.getColumn("Action").setCellRenderer(centerRenderer);

			tableUsers.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					scrollPaneUsers.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			});

			tableUsers.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int row = tableUsers.rowAtPoint(e.getPoint());
					int col = tableUsers.columnAtPoint(e.getPoint());
					if (row >= 0 && col == tableUsers.getColumn("Action").getModelIndex()
							&& tableUsers.getValueAt(row, col) == "Kick Out") {
						scrollPaneUsers.setCursor(new Cursor(Cursor.HAND_CURSOR));
					} else {
						scrollPaneUsers.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}

		tableUsers.setEnabled(false);
		scrollPaneUsers.setViewportView(tableUsers);
	}

	public void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(frmMain, msg);
	}

	public int showConfirmDialog(String msg) {
		return JOptionPane.showConfirmDialog(frmMain, msg, "Confirm", JOptionPane.OK_CANCEL_OPTION);
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

	public class MultilineTableCell implements TableCellRenderer {
		class CellArea extends DefaultTableCellRenderer {
			private static final long serialVersionUID = 1L;
			private String text;
			protected int rowIndex;
			protected int columnIndex;
			protected JTable table;
			protected Font font;
			private int paragraphStart, paragraphEnd;
			private LineBreakMeasurer lineMeasurer;

			public CellArea(String s, JTable tab, int row, int column, boolean isSelected) {
				text = s;
				rowIndex = row;
				columnIndex = column;
				table = tab;
				font = table.getFont();
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				}
			}

			public void paintComponent(Graphics gr) {
				super.paintComponent(gr);
				if (text != null && !text.isEmpty()) {
					Graphics2D g = (Graphics2D) gr;
					if (lineMeasurer == null) {
						AttributedCharacterIterator paragraph = new AttributedString(text).getIterator();
						paragraphStart = paragraph.getBeginIndex();
						paragraphEnd = paragraph.getEndIndex();
						FontRenderContext frc = g.getFontRenderContext();
						lineMeasurer = new LineBreakMeasurer(paragraph, BreakIterator.getWordInstance(), frc);
					}
					float breakWidth = (float) table.getColumnModel().getColumn(columnIndex).getWidth();
					float drawPosY = 0;
					// Set position to the index of the first character in the paragraph.
					lineMeasurer.setPosition(paragraphStart);
					// Get lines until the entire paragraph has been displayed.
					while (lineMeasurer.getPosition() < paragraphEnd) {
						// Retrieve next layout. A cleverer program would also cache
						// these layouts until the component is re-sized.
						TextLayout layout = lineMeasurer.nextLayout(breakWidth);
						// Compute pen x position. If the paragraph is right-to-left we
						// will align the TextLayouts to the right edge of the panel.
						// Note: this won't occur for the English text in this sample.
						// Note: drawPosX is always where the LEFT of the text is placed.
						float drawPosX = layout.isLeftToRight() ? 0 : breakWidth - layout.getAdvance();
						// Move y-coordinate by the ascent of the layout.
						drawPosY += layout.getAscent();
						// Draw the TextLayout at (drawPosX, drawPosY).
						layout.draw(g, drawPosX, drawPosY);
						// Move y-coordinate in preparation for next layout.
						drawPosY += layout.getDescent() + layout.getLeading();
					}
					table.setRowHeight(rowIndex, (int) drawPosY);
				}
			}
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			CellArea area = new CellArea(value.toString(), table, row, column, isSelected);
			return area;
		}
	}
}
