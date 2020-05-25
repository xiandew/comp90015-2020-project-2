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

	private Graphics2D eraserG2d;

	private Color bgColor = Color.WHITE;
	private Color paintColor = Color.BLACK;

	public Board() {
		ShapePainter shapePainter = new ShapePainter();
		addMouseListener(shapePainter);
		addMouseMotionListener(shapePainter);

		TextPainter textPainter = new TextPainter();
		addMouseListener(textPainter);

		Eraser eraser = new Eraser();
		addMouseListener(eraser);
		addMouseMotionListener(eraser);
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

	public void clear(Graphics2D g) {
		g.setPaint(bgColor);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setPaint(paintColor);
		repaint();
	}

	class TextPainter extends MouseAdapter {
		private JTextField tf;

		@Override
		public void mouseClicked(MouseEvent e) {
			if (selectedOperation == BoardOperation.TEXT && tf == null) {
				// Create a textField on the board for the user to enter a string
				tf = new JTextField();
				tf.setBounds(e.getX(), e.getY(), 300, 24);
				Board.this.add(tf);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (tf != null) {
				// Draw the text onto the shared canvas and remove the textField
				canvasG2d.drawString(tf.getText(), tf.getX(), tf.getY());
				Board.this.remove(tf);
				tf = null;

				clear(canvasBufferG2d);
				canvasBufferG2d.drawImage(canvas, 0, 0, null);
				repaint();
			}
		}
	}

	class ShapePainter extends MouseAdapter {
		protected Point startPoint;
		protected Point endPoint;

		public ShapePainter() {
			super();
			startPoint = new Point();
			endPoint = new Point();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			setStartPoint(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());

			clear(canvasBufferG2d);
			canvasBufferG2d.drawImage(canvas, 0, 0, null);

			drawShape(canvasBufferG2d);

			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());

			// Propagate changes onto the shared canvas
			drawShape(canvasG2d);

			clear(canvasBufferG2d);
			canvasBufferG2d.drawImage(canvas, 0, 0, null);

			repaint();
		}

		public void setStartPoint(int x, int y) {
			startPoint.x = x;
			startPoint.y = y;
		}

		public void setEndPoint(int x, int y) {
			endPoint.x = x;
			endPoint.y = y;
		}

		public void drawShape(Graphics2D g) {
			switch (selectedOperation) {
			case LINE:
				drawLine(g);
				break;
			case CIRCLE:
				drawCircle(g);
				break;
			case RECT:
				drawRect(g);
				break;
			default:
				break;
			}
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
	}

	class Eraser extends ShapePainter {
		@Override
		public void mouseDragged(MouseEvent e) {
			setEndPoint(e.getX(), e.getY());

			if (selectedOperation == BoardOperation.ERASER) {
				eraserG2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

				// Update the start point for free drawing
				startPoint.x = endPoint.x;
				startPoint.y = endPoint.y;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Do nothing
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
