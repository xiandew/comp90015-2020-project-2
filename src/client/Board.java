package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class Board extends JPanel {
	private static final long serialVersionUID = 1L;

	public BoardOperation selectedOperation = BoardOperation.LINE;

	// This canvas will have both the shared canvas and the dragging preview
	private Image canvasBuffer;
	private Graphics2D canvasBufferG2d;

	// This canvas will have the shared view
	private Image canvas;
	private Graphics2D canvasG2d;

	// For eraser
	private Graphics2D eraserG2d;

	private Color bgColor = Color.WHITE;
	private Color paintColor = Color.BLACK;

	private Point startPoint;
	private Point endPoint;

	// TextField for drawString
	private JTextField tf;

	public Board() {
		startPoint = new Point();
		endPoint = new Point();
		BoardMouseListener listener = new BoardMouseListener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}

	public void initCanvas() {
		canvasBuffer = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		canvasBufferG2d = (Graphics2D) canvasBuffer.getGraphics();
		this.clear(canvasBufferG2d);

		canvas = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		canvasG2d = (Graphics2D) canvas.getGraphics();
		this.clear(canvasG2d);

		eraserG2d = (Graphics2D) canvasG2d.create();
		eraserG2d.setPaint(bgColor);
		eraserG2d.setStroke(new BasicStroke(10));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(canvasBuffer, 0, 0, null);
	}

	public void drawLine(Graphics2D g) {
		g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
	}

	public void drawCircle(Graphics2D g) {
		int diameter = Math.min(Math.abs(startPoint.x - endPoint.x), Math.abs(startPoint.y - endPoint.y));
		g.drawOval(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y), diameter, diameter);
	}

	public void drawRect(Graphics2D g) {
		g.drawRect(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y),
				Math.abs(startPoint.x - endPoint.x), Math.abs(startPoint.y - endPoint.y));
	}

	/**
	 * Create a textField on the board for the user to enter a string
	 * 
	 * @param x
	 * @param y
	 */
	public void addTextField(int x, int y) {
		tf = new JTextField();
		tf.setBounds(x, y, 300, 24);
		this.add(tf);
	}

	/**
	 * Draw the text onto the shared canvas and remove the textField
	 * 
	 * @param g Graphics object to draw with
	 */
	public void drawString(Graphics2D g) {
		g.drawString(tf.getText(), tf.getX(), tf.getY());
		this.remove(tf);
		tf = null;
	}

	public void erase() {
		eraserG2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

		// Update the start point for free drawing
		startPoint.x = endPoint.x;
		startPoint.y = endPoint.y;
	}

	public void clear(Graphics2D g) {
		g.setPaint(bgColor);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setPaint(paintColor);
		repaint();
	}

	class BoardMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			setStartPoint(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());

			clear(canvasBufferG2d);
			canvasBufferG2d.drawImage(canvas, 0, 0, null);

			switch (selectedOperation) {
			case LINE:
				drawLine(canvasBufferG2d);
				break;
			case CIRCLE:
				drawCircle(canvasBufferG2d);
				break;
			case RECT:
				drawRect(canvasBufferG2d);
				break;
			case ERASER:
				erase();
				break;
			default:
				// Do nothing
			}

			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());

			// Propagate changes onto the shared canvas
			switch (selectedOperation) {
			case LINE:
				drawLine(canvasG2d);
				break;
			case CIRCLE:
				drawCircle(canvasG2d);
				break;
			case RECT:
				drawRect(canvasG2d);
				break;
			default:
				// Do nothing
			}

			clear(canvasBufferG2d);
			canvasBufferG2d.drawImage(canvas, 0, 0, null);
			repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (selectedOperation == BoardOperation.TEXT) {
				if (tf == null) {
					addTextField(e.getX(), e.getY());
				} else {
					drawString(canvasG2d);
					clear(canvasBufferG2d);
					canvasBufferG2d.drawImage(canvas, 0, 0, null);
					repaint();
				}
			}
		}

		public void setStartPoint(int x, int y) {
			startPoint.x = x;
			startPoint.y = y;
		}

		public void setEndPoint(int x, int y) {
			endPoint.x = x;
			endPoint.y = y;
		}
	}

	class Point {
		public int x;
		public int y;
	}

	public static enum BoardOperation {
		LINE, CIRCLE, RECT, TEXT, ERASER;
	}
}
