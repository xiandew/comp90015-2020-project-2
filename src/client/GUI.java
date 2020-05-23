package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.border.LineBorder;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI {

	private JFrame frmMain;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmMain.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
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

		JPanel panelWhiteBoard = new Board();
		panelWhiteBoard.setBorder(new LineBorder(new Color(0, 0, 0)));
		panelWhiteBoard.setBackground(Color.WHITE);
		panelWhiteBoard.setBounds(14, 13, 619, 551);
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

		ButtonGroup buttonGroupBoardOperators = new ButtonGroup();

		JRadioButton rdbtnRect = new JRadioButton("Rect");
		rdbtnRect.setBounds(164, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnRect);
		buttonGroupBoardOperators.add(rdbtnRect);

		JRadioButton rdbtnCircle = new JRadioButton("Circle");
		rdbtnCircle.setBounds(81, 577, 77, 27);
		frmMain.getContentPane().add(rdbtnCircle);
		buttonGroupBoardOperators.add(rdbtnCircle);

		JRadioButton rdbtnLine = new JRadioButton("Line");
		rdbtnLine.setSelected(true);
		rdbtnLine.setBounds(14, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnLine);
		buttonGroupBoardOperators.add(rdbtnLine);

		JRadioButton rdbtnText = new JRadioButton("Text");
		rdbtnText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		rdbtnText.setBounds(231, 577, 61, 27);
		frmMain.getContentPane().add(rdbtnText);
		buttonGroupBoardOperators.add(rdbtnText);
		
		JRadioButton rdbtnEraser = new JRadioButton("Eraser");
		rdbtnEraser.setBounds(298, 577, 77, 27);
		frmMain.getContentPane().add(rdbtnEraser);
		buttonGroupBoardOperators.add(rdbtnEraser);
	}
}
