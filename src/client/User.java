package client;

import java.awt.Point;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONObject;

import client.GUI.GUI;
import remote.ICollaborator;
import remote.IMediator;
import utils.ActionType;

public class User extends UnicastRemoteObject implements ICollaborator {
	private static final long serialVersionUID = 1L;

	protected IMediator mediator;
	protected int id;
	private String username;
	protected GUI gui;

	protected User(String username, String serverAddress, int serverPort) throws RemoteException {
		this.username = username;
		this.gui = new GUI();

		try {
			this.connect(serverAddress, serverPort);
		} catch (NotBoundException e) {
			System.out.println("The remote object not bound");
			System.exit(0);
		}

		this.updateBoard();
		this.updateUserList();
		this.broadcastsUserListUpdate();

		System.out.println("Successfully joined the white board");
		this.launchGUI();

		// Listen for board actions and broadcast if any
		new Thread(() -> {
			while (true) {
				try {
					JSONObject data = this.gui.board.boardActionQueue.remove();
					this.broadcastBoardActions(data.toString());
				} catch (NoSuchElementException e) {
					// Queue is empty, do nothing
				} catch (RemoteException e) {
					GUI.showMessageDialog("Unable to broadcast messages");
					break;
				}
			}
		}).start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				this.unregister();
				this.broadcastsUserListUpdate();
			} catch (RemoteException e) {
				// Do nothing
			}
		}));
	}

	public void launchGUI() {
		this.gui.launch();
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public void connect(String serverAddress, int serverPort) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(serverAddress, serverPort);
		this.mediator = (IMediator) registry.lookup(IMediator.REMOTE_OBJECT_NAME);
		int id = this.mediator.register(this);
		if (id < 0) {
			System.out.println("No white board created yet. Please create one instead");
			System.exit(0);
		}
		this.id = id;
	}

	@Override
	public void updateBoard() throws RemoteException {
		JSONArray jBoardActions = new JSONArray();
		for (String boardAction : this.mediator.getExecutedBoardActions()) {
			jBoardActions.put(boardAction);
		}
		this.refreshBoard(jBoardActions);
	}

	@Override
	public void updateUserList() throws RemoteException {
		LinkedList<String> users = this.mediator.getUsers();
		LinkedList<JSONObject> jUsers = new LinkedList<>();
		for (String user : users) {
			JSONObject jUser = new JSONObject(user);
			boolean isManager = Boolean.parseBoolean((String) jUser.get("isManager"));
			int id = Integer.parseInt((String) jUser.get("id"));
			int sortCriteria = id;
			String displayNameFmt = "[%d] %s";
			if (isManager) {
				sortCriteria = -2;
				displayNameFmt = "[%d] %s (Manager)";
			}
			if (id == this.id) {
				sortCriteria = -1;
				displayNameFmt = "[%d] %s (You)";
			}
			jUser.put("sortCriteria", sortCriteria);
			jUser.put("displayName", String.format(displayNameFmt, id, jUser.get("username")));
			jUsers.add(jUser);
		}
		Collections.sort(jUsers, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject a, JSONObject b) {
				return ((int) a.get("sortCriteria")) - ((int) b.get("sortCriteria"));
			}
		});
		this.gui.updateUserList(jUsers);
	}

	@Override
	public void unregister() throws RemoteException {
		try {
			this.mediator.removeUser(this.id);
		} catch (Exception e) {
			// Do nothing
		}
	}

	@Override
	public void send(String data, int to) throws RemoteException {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void broadcast(String data) throws RemoteException {
		this.mediator.broadcast(data, this.id);
	}

	public void broadcastBoardActions(String data) throws RemoteException {
		this.mediator.broadcastAndAddBoardAction(data, this.id);
	}

	public void broadcastsUserListUpdate() throws RemoteException {
		// Broadcast messages
		JSONObject data = new JSONObject();
		data.put("actionType", ActionType.USER_LIST_UPDATE.toString());
		this.broadcast(data.toString());
	}

	@Override
	public void notify(String data, int from) throws RemoteException {
		if (from == this.id) {
			return;
		}

		JSONObject jData = new JSONObject(data);
		ActionType actionType = ActionType.valueOf((String) jData.get("actionType"));
		switch (actionType) {
		case TEXT:
		case LINE:
		case CIRCLE:
		case RECT:
		case ERASER:
			boolean ifRepaint = (!jData.has("ifRepaint")) || Boolean.parseBoolean((String) jData.get("ifRepaint"));
			if (actionType == ActionType.TEXT) {
				JSONObject jPoint = new JSONObject(jData.get("point").toString());
				Point point = new Point((int) jPoint.get("x"), (int) jPoint.get("y"));
				this.gui.board.drawString(point, (String) jData.get("text"), ifRepaint);
				break;
			}

			JSONObject jStartPoint = new JSONObject(jData.get("startPoint").toString());
			JSONObject jEndPoint = new JSONObject(jData.get("endPoint").toString());
			Point startPoint = new Point((int) jStartPoint.get("x"), (int) jStartPoint.get("y"));
			Point endPoint = new Point((int) jEndPoint.get("x"), (int) jEndPoint.get("y"));
			if (actionType == ActionType.ERASER) {
				this.gui.board.erase(startPoint, endPoint, ifRepaint);
				break;
			}

			this.gui.board.drawShape(startPoint, endPoint, actionType, ifRepaint);
			break;
		case USER_LIST_UPDATE:
			this.updateUserList();
			break;
		case MANAGER_EXIT:
			GUI.showMessageDialog("The manager closed the board");
			System.exit(0);
			break;
		case FILE_NEW:
			this.refreshBoard();
			break;
		case FILE_OPEN:
			this.refreshBoard(new JSONArray(jData.get("boardActions").toString()));
			break;
		default:
			break;
		}
	}

	protected void refreshBoard() {
		this.gui.board.clear();
	}

	protected void refreshBoard(JSONArray boardActions) throws RemoteException {
		// Repaint only after performing all the board actions from the file, otherwise
		// the drawing will be lagged to the users
		for (Object boardAction : boardActions) {
			JSONObject jBoardAction = new JSONObject(boardAction.toString());
			jBoardAction.put("ifRepaint", Boolean.toString(false));
			this.notify(jBoardAction.toString(), -1);
		}
		this.gui.board.clearAndRepaint();
	}

	public static void main(String[] args) {
		String serverAddress = null;
		Integer serverPort = null;
		String username = null;

		try {
			serverAddress = args[0];
			serverPort = Integer.parseInt(args[1]);
			username = args[2];

		} catch (Exception e) {
			System.out.println("Usage: java -jar JoinWhiteBoard.jar <serverAddress> <serverPort> username");
			System.exit(0);
		}

		try {
			new User(username, serverAddress, serverPort);
		} catch (RemoteException e) {
			System.out.println("Cannot connect to the server");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
