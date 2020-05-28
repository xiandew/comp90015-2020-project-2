package client.GUI;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.border.LineBorder;

import remote.ICollaborator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class GUI {

	private JFrame frmMain;
	private Board panelWhiteBoard;

	public GUI(ICollaborator c) {
		this.panelWhiteBoard = new Board(c);
		initialize();
	}

	public void launch() {
		this.frmMain.setVisible(true);
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
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMain.getContentPane().setLayout(null);

		panelWhiteBoard.setBorder(new LineBorder(new Color(0, 0, 0)));
		panelWhiteBoard.setBounds(14, 13, 619, 551);
		panelWhiteBoard.initCanvas();
		frmMain.getContentPane().add(panelWhiteBoard);
		panelWhiteBoard.setLayout(null);

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

		JPanel panelUsers = new JPanel();
		panelUsers.setBounds(647, 315, 310, 302);
		frmMain.getContentPane().add(panelUsers);
		panelUsers.setLayout(null);

		JLabel lblUsers = new JLabel("Users");
		lblUsers.setBounds(14, 0, 72, 18);
		panelUsers.add(lblUsers);

		JScrollPane scrollPaneUsers = new JScrollPane();
		scrollPaneUsers.setBounds(14, 23, 282, 266);
		panelUsers.add(scrollPaneUsers);

		ButtonGroup buttonGroupBoardActions = new ButtonGroup();

		JRadioButton rdbtnLine = new JRadioButton(Board.BoardAction.LINE.toString());
		rdbtnLine.setSelected(true);
		rdbtnLine.setBounds(14, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnLine);
		buttonGroupBoardActions.add(rdbtnLine);

		JRadioButton rdbtnCircle = new JRadioButton(Board.BoardAction.CIRCLE.toString());
		rdbtnCircle.setBounds(81, 577, 77, 27);
		frmMain.getContentPane().add(rdbtnCircle);
		buttonGroupBoardActions.add(rdbtnCircle);

		JRadioButton rdbtnRect = new JRadioButton(Board.BoardAction.RECT.toString());
		rdbtnRect.setBounds(164, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnRect);
		buttonGroupBoardActions.add(rdbtnRect);

		JRadioButton rdbtnText = new JRadioButton(Board.BoardAction.TEXT.toString());
		rdbtnText.setBounds(231, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnText);
		buttonGroupBoardActions.add(rdbtnText);

		JRadioButton rdbtnEraser = new JRadioButton(Board.BoardAction.ERASER.toString());
		rdbtnEraser.setBounds(298, 577, 77, 27);
		frmMain.getContentPane().add(rdbtnEraser);
		buttonGroupBoardActions.add(rdbtnEraser);

		ActionListener selectBoardAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panelWhiteBoard.selectedAction = Board.BoardAction.valueOf(((JRadioButton) e.getSource()).getText());
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
