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
	protected GUI gui;
	protected int id;
	private String username;
	private boolean isJoined;

	protected User(String username, String serverAddress, int serverPort) throws RemoteException {
		this.username = username;
		this.isJoined = false;
		this.gui = new GUI();

		try {
			this.connect(serverAddress, serverPort);
		} catch (NotBoundException e) {
			System.out.println("The remote object not bound");
			System.exit(0);
		}

		// Listen for board actions and broadcast if any
		new Thread(() -> {
			while (true) {
				try {
					JSONObject data = this.gui.board.boardActionQueue.remove();
					this.broadcastBoardActions(data.toString());
				} catch (NoSuchElementException e) {
					// Queue is empty, do nothing
				} catch (RemoteException e) {
					this.gui.showMessageDialog("Unable to broadcast messages");
					break;
				}
			}
		}).start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				this.unregister();
			} catch (RemoteException e) {
				// Do nothing
			}
		}));
	}

	public void launchGUI() {
		this.gui.launch();
	}

	@Override
	public int getId() throws RemoteException {
		return this.id;
	}

	@Override
	public String getUsername() throws RemoteException {
		return this.username;
	}

	@Override
	public boolean getIsJoined() throws RemoteException {
		return this.isJoined;
	}

	@Override
	public void connect(String serverAddress, int serverPort) throws RemoteException, NotBoundException {
		System.out.println("Connecting...");

		Registry registry = LocateRegistry.getRegistry(serverAddress, serverPort);
		this.mediator = (IMediator) registry.lookup(IMediator.REMOTE_OBJECT_NAME);
		int managerId = this.mediator.getManagerId();
		if (managerId < 0) {
			System.out.println("No white board created yet. Please create one instead");
			System.exit(0);
		}

		this.id = this.mediator.register(this);
		System.out.println("Connected. Please wait the manager to approve your request...");

		new Thread(() -> {
			// Notify manager of the join request
			JSONObject data = new JSONObject();
			data.put("actionType", ActionType.JOIN_REQUEST);
			data.put("id", this.id);
			data.put("username", this.username);
			try {
				this.send(data.toString(), managerId);
			} catch (RemoteException e) {
				System.out.println("Unable to send the join request");
				System.exit(0);
			}
		}).start();
	}

	@Override
	public void updateBoard() throws RemoteException {
		JSONArray jBoardActions = new JSONArray();
		for (String boardAction : this.mediator.getExecutedBoardActions()) {
			jBoardActions.put(boardAction);
		}
		this.refreshBoard(jBoardActions);
	}

	public LinkedList<JSONObject> getUserList() throws RemoteException {
		LinkedList<String> users = this.mediator.getUsers();
		LinkedList<JSONObject> jUsers = new LinkedList<>();
		for (String user : users) {
			JSONObject jUser = new JSONObject(user);
			boolean isManager = Boolean.parseBoolean((String) jUser.get("isManager"));
			int id = Integer.parseInt((String) jUser.get("id"));
			int sortCriteria = id;
			String displayNameFmt = "%s";
			if (isManager) {
				sortCriteria = -2;
				displayNameFmt = "%s (Manager)";
			}
			if (id == this.id) {
				sortCriteria = -1;
				displayNameFmt = "%s (You)";
			}
			jUser.put("sortCriteria", sortCriteria);
			jUser.put("displayName", String.format(displayNameFmt, jUser.get("username")));
			jUsers.add(jUser);
		}
		Collections.sort(jUsers, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject a, JSONObject b) {
				return ((int) a.get("sortCriteria")) - ((int) b.get("sortCriteria"));
			}
		});
		return jUsers;
	}

	@Override
	public void unregister() throws RemoteException {
		try {
			this.mediator.removeUser(this.id);
			this.broadcastUserListUpdate();
		} catch (Exception e) {
			// Do nothing
		}
	}

	@Override
	public void send(String data, int to) throws RemoteException {
		this.mediator.send(data, to, this.id);
	}

	@Override
	public void broadcast(String data) throws RemoteException {
		this.mediator.broadcast(data, this.id);
	}

	public void broadcastBoardActions(String data) throws RemoteException {
		this.mediator.addBoardActions(data);
		this.broadcast(data);
	}

	public void broadcastUserListUpdate() throws RemoteException {
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
		case JOIN_REQUEST_APPROVED:
			this.isJoined = true;
			this.updateBoard();
			this.updateUserList();
			this.broadcastUserListUpdate();
			this.launchGUI();
			break;
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
		case FILE_NEW:
			this.refreshBoard();
			break;
		case FILE_OPEN:
			this.refreshBoard(new JSONArray(jData.get("boardActions").toString()));
			break;
		case JOIN_REQUEST_DECLINED:
		case KICKED_OUT:
		case MANAGER_EXIT:
		case SERVER_SHUTDOWN:
			switch (actionType) {
			case JOIN_REQUEST_DECLINED:
				this.gui.showMessageDialog("The manager declined your join request");
				break;
			case KICKED_OUT:
				this.gui.showMessageDialog("You are kicked out by the manager");
				break;
			case MANAGER_EXIT:
				this.gui.showMessageDialog("The manager closed the board");
				break;
			case SERVER_SHUTDOWN:
				this.gui.showMessageDialog("The server has been shutdown");
				break;
			default:
				break;
			}
			// Exit the program in another thread as it may cause connection reset exception
			// if not
			new Thread(() -> {
				System.exit(0);
			}).start();
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

	public void updateUserList() throws RemoteException {
		this.gui.updateUserList(this.getUserList(), false);
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
