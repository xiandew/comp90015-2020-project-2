package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Board extends JPanel {
	private static final long serialVersionUID = 1L;

	// This canvas will have both canvasBuffer and the dragging preview drawn
	private Image canvas;
	private Graphics2D canvasG2d;

	private Image canvasBuffer;
	private Graphics2D canvasBufferG2d;

	private Point startPoint;
	private Point endPoint;

	public Board() {
		startPoint = new Point();
		endPoint = new Point();
		BoardMouseListener listener = new BoardMouseListener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}

	public void initCanvas() {
		canvas = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		canvasG2d = (Graphics2D) canvas.getGraphics();
		clear(canvasG2d);

		canvasBuffer = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		canvasBufferG2d = (Graphics2D) canvasBuffer.getGraphics();
		clear(canvasBufferG2d);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(canvas, 0, 0, null);
	}

	public void clear(Graphics2D g) {
		g.setPaint(Color.WHITE);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setPaint(Color.black);
		repaint();
	}

	public void drawRect(Graphics g) {
		g.drawRect(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y),
				Math.abs(startPoint.x - endPoint.x), Math.abs(startPoint.y - endPoint.y));
	}

	public void setStartPoint(int x, int y) {
		startPoint.x = x;
		startPoint.y = y;
	}

	public void setEndPoint(int x, int y) {
		endPoint.x = x;
		endPoint.y = y;
	}

	class Point {
		public int x;
		public int y;
	}

	class BoardMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			setStartPoint(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());

			clear(canvasG2d);
			canvasG2d.drawImage(canvasBuffer, 0, 0, null);
			// TODO Draw shapes accordingly
			drawRect(canvasG2d);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());
			// TODO Draw shapes accordingly
			drawRect(canvasBufferG2d);
			repaint();
		}
	}
}
