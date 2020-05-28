package client.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.JSONObject;

import remote.ICollaborator;

public class Board extends JPanel {
	private static final long serialVersionUID = 1L;

	// This canvas will have both the shared canvas and the dragging preview
	private Image canvasBuffer;
	private Graphics2D canvasBufferG2d;

	// This canvas will have the shared view
	private Image canvas;
	private Graphics2D canvasG2d;

	private Graphics2D eraserG2d;

	private Color bgColor = Color.WHITE;
	private Color paintColor = Color.BLACK;

	public BoardAction selectedAction = BoardAction.LINE;

	// Data to be broadcasted
	private LinkedList<JSONObject> broadcastingQueue = new LinkedList<>();

	public Board(ICollaborator c) {
		ShapePainter shapePainter = new ShapePainter();
		addMouseListener(shapePainter);
		addMouseMotionListener(shapePainter);

		TextPainter textPainter = new TextPainter();
		addMouseListener(textPainter);

		Eraser eraser = new Eraser();
		addMouseListener(eraser);
		addMouseMotionListener(eraser);

		new Thread(() -> {
			while (true) {
				try {
					JSONObject data = broadcastingQueue.remove();
					c.broadcast(data.toString());
				} catch (NoSuchElementException e) {
					// Queue is empty, do nothing
				} catch (RemoteException e) {
					e.printStackTrace();
					GUI.showMessageDialog("Unable to broadcast messages");
					break;
				}
			}
		}).start();
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

	private void clear(Graphics2D g) {
		g.setPaint(bgColor);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setPaint(paintColor);
		repaint();
	}

	private void drawLine(Graphics2D g, Point startPoint, Point endPoint) {
		g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
	}

	private void drawCircle(Graphics2D g, Point startPoint, Point endPoint) {
		int diameter = Math.min(Math.abs(startPoint.x - endPoint.x), Math.abs(startPoint.y - endPoint.y));
		g.drawOval(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y), diameter, diameter);
	}

	private void drawRect(Graphics2D g, Point startPoint, Point endPoint) {
		g.drawRect(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y),
				Math.abs(startPoint.x - endPoint.x), Math.abs(startPoint.y - endPoint.y));
	}

	private void drawShape(Graphics2D g, Point startPoint, Point endPoint, BoardAction shape) {
		switch (shape) {
		case LINE:
			drawLine(g, startPoint, endPoint);
			break;
		case CIRCLE:
			drawCircle(g, startPoint, endPoint);
			break;
		case RECT:
			drawRect(g, startPoint, endPoint);
			break;
		default:
			break;
		}
	}

	private void drawShape(Graphics2D g, Point startPoint, Point endPoint) {
		drawShape(g, startPoint, endPoint, selectedAction);
	}

	public void drawShape(Point startPoint, Point endPoint, BoardAction shape) {
		drawShape(canvasG2d, startPoint, endPoint, shape);
	}

	public void erase(Point startPoint, Point endPoint) {
		eraserG2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
	}

	public void drawString(Point point, String string) {
		canvasG2d.drawString(string, point.x, point.y);
	}

	class TextPainter extends MouseAdapter {
		private JTextField tf;

		@Override
		public void mouseClicked(MouseEvent e) {
			if (selectedAction == BoardAction.TEXT && tf == null) {
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
				Point point = new Point(tf.getX(), tf.getY());
				drawString(point, tf.getText());
				Board.this.remove(tf);
				tf = null;

				clear(canvasBufferG2d);
				canvasBufferG2d.drawImage(canvas, 0, 0, null);
				repaint();

				// Broadcast messages
				Board.this.broadcastingQueue.add(new JSONObject(String.format(
						"{type: 'BoardAction', boardAction: 'TEXT', point: {x: %d, y: %d}}", point.x, point.y)));
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
			startPoint = e.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			endPoint = e.getPoint();

			clear(canvasBufferG2d);
			canvasBufferG2d.drawImage(canvas, 0, 0, null);

			drawShape(canvasBufferG2d, startPoint, endPoint);

			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			endPoint = e.getPoint();

			// Propagate changes onto the shared canvas
			drawShape(canvasG2d, startPoint, endPoint);

			clear(canvasBufferG2d);
			canvasBufferG2d.drawImage(canvas, 0, 0, null);

			repaint();
		}
	}

	class Eraser extends ShapePainter {
		@Override
		public void mouseDragged(MouseEvent e) {
			endPoint = e.getPoint();

			if (selectedAction == BoardAction.ERASER) {
				erase(startPoint, endPoint);

				// Update the start point for free drawing
				startPoint = endPoint;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Do nothing
		}
	}

	public static enum BoardAction {
		LINE, CIRCLE, RECT, TEXT, ERASER;
	}
}
