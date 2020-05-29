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
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.JSONObject;

import utils.ActionType;

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

	public ActionType selectedAction = ActionType.LINE;

	// Data to be broadcasted
	public LinkedList<JSONObject> boardActionQueue = new LinkedList<>();

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
		this.canvasBuffer = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		this.canvasBufferG2d = (Graphics2D) this.canvasBuffer.getGraphics();
		this.clear(this.canvasBufferG2d);

		this.canvas = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		this.canvasG2d = (Graphics2D) this.canvas.getGraphics();
		this.clear(this.canvasG2d);

		this.eraserG2d = (Graphics2D) this.canvasG2d.create();
		this.eraserG2d.setPaint(this.bgColor);
		this.eraserG2d.setStroke(new BasicStroke(10));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(canvasBuffer, 0, 0, null);
	}

	public void clear() {
		this.clear(canvasBufferG2d);
		this.clear(canvasG2d);
		repaint();
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

	private void drawShape(Graphics2D g, Point startPoint, Point endPoint, ActionType shape) {
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

	public void clearAndRepaint() {
		clear(canvasBufferG2d);
		canvasBufferG2d.drawImage(canvas, 0, 0, null);
		repaint();
	}

	public void drawShape(Point startPoint, Point endPoint, ActionType shape) {
		this.drawShape(canvasG2d, startPoint, endPoint, shape);
		this.clearAndRepaint();
	}

	public void drawShape(Point startPoint, Point endPoint, ActionType shape, boolean ifRepaint) {
		this.drawShape(canvasG2d, startPoint, endPoint, shape);
		if (ifRepaint) {
			this.clearAndRepaint();
		}
	}

	public void erase(Point startPoint, Point endPoint) {
		eraserG2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		this.clearAndRepaint();
	}

	public void erase(Point startPoint, Point endPoint, boolean ifRepaint) {
		eraserG2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		if (ifRepaint) {
			this.clearAndRepaint();
		}
	}

	public void drawString(Point point, String string) {
		canvasG2d.drawString(string, point.x, point.y);
		this.clearAndRepaint();
	}

	public void drawString(Point point, String string, boolean ifRepaint) {
		this.drawString(point, string);
		if (ifRepaint) {
			this.clearAndRepaint();
		}
	}

	class TextPainter extends MouseAdapter {
		private JTextField tf;

		@Override
		public void mouseClicked(MouseEvent e) {
			if (selectedAction == ActionType.TEXT && tf == null) {
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
				String text = tf.getText();
				drawString(point, text);
				Board.this.remove(tf);
				tf = null;

				// Broadcast messages
				HashMap<String, String> data = new HashMap<>();
				data.put("actionType", ActionType.TEXT.toString());
				data.put("text", text);
				data.put("point", String.format("{'x': %d, 'y': %d}", point.x, point.y));
				Board.this.boardActionQueue.add(new JSONObject(data));
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
			drawShape(canvasBufferG2d, startPoint, endPoint, selectedAction);
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			endPoint = e.getPoint();

			// Propagate changes onto the shared canvas
			drawShape(startPoint, endPoint, selectedAction);

			switch (selectedAction) {
			case LINE:
			case CIRCLE:
			case RECT:
				// Broadcast messages
				HashMap<String, String> data = new HashMap<>();
				data.put("actionType", selectedAction.toString());
				data.put("startPoint", String.format("{'x': %d, 'y': %d}", startPoint.x, startPoint.y));
				data.put("endPoint", String.format("{'x': %d, 'y': %d}", endPoint.x, endPoint.y));
				Board.this.boardActionQueue.add(new JSONObject(data));
				break;
			default:
				break;
			}
		}
	}

	class Eraser extends ShapePainter {
		@Override
		public void mouseDragged(MouseEvent e) {
			endPoint = e.getPoint();

			if (selectedAction == ActionType.ERASER) {
				erase(startPoint, endPoint);

				// Broadcast messages
				HashMap<String, String> data = new HashMap<>();
				data.put("actionType", ActionType.ERASER.toString());
				data.put("startPoint", String.format("{'x': %d, 'y': %d}", startPoint.x, startPoint.y));
				data.put("endPoint", String.format("{'x': %d, 'y': %d}", endPoint.x, endPoint.y));
				Board.this.boardActionQueue.add(new JSONObject(data));

				// Update the start point for free drawing
				startPoint = endPoint;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Do nothing
		}
	}
}
