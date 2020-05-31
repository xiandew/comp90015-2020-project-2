package server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import remote.ICollaborator;
import remote.IMediator;
import utils.ActionType;

public class Mediator extends UnicastRemoteObject implements IMediator {
	private static final long serialVersionUID = 1L;

	private ConcurrentLinkedQueue<String> executedBoardActions = new ConcurrentLinkedQueue<>();
	private ConcurrentHashMap<Integer, ICollaborator> users = new ConcurrentHashMap<>();
	private ICollaborator manager;
	private int nUsers = 0;

	protected Mediator() throws RemoteException {
		super();
	}

	@Override
	public int register(ICollaborator c) throws RemoteException {
		int id = nUsers++;
		this.users.put(id, c);
		return id;
	}

	@Override
	public int registerAsManager(ICollaborator m) throws RemoteException {
		if (this.manager != null) {
			return -1;
		}
		this.manager = m;
		return this.register(m);
	}

	@Override
	public ConcurrentLinkedQueue<String> getExecutedBoardActions() throws RemoteException {
		return this.executedBoardActions;
	}

	@Override
	public void removeUser(int id) throws RemoteException {
		this.users.remove(id);
	}

	@Override
	public void removeManager() throws RemoteException {
		this.users.remove(manager.getId());
		manager = null;
	}

	@Override
	public int getManagerId() throws RemoteException {
		return manager == null ? -1 : manager.getId();
	}

	@Override
	public LinkedList<String> getUsers() throws RemoteException {
		LinkedList<String> jUsers = new LinkedList<>();
		for (ICollaborator user : this.users.values()) {
			HashMap<String, String> jUser = new HashMap<>();
			jUser.put("id", Integer.toString(user.getId()));
			jUser.put("username", user.getUsername());
			jUser.put("isManager", Boolean.toString(manager != null && user.getId() == manager.getId()));
			jUsers.add(new JSONObject(jUser).toString());
		}
		return jUsers;
	}

	@Override
	public void send(String data, int to, int from) throws RemoteException {
		this.users.get(to).notify(data, from);
	}

	@Override
	public void broadcast(String data, int from) throws RemoteException {
		for (int id : this.users.keySet()) {
			try {
				this.users.get(id).notify(data, from);
			} catch (RemoteException e) {
				// The user might be disconnected
				this.removeUser(id);
			}
		}
	}

	@Override
	public void addBoardActions(String data) throws RemoteException {
		this.executedBoardActions.add(data);
	}

	@Override
	public void resetBoardActions() throws RemoteException {
		this.executedBoardActions.clear();
	}

	@Override
	public void resetBoardActions(String boardActions) throws RemoteException {
		this.resetBoardActions();
		for (Object jBoardAction : new JSONArray(boardActions)) {
			this.executedBoardActions.add(jBoardAction.toString());
		}
	}

	public static void main(String[] args) {

		Integer serverPort = null;
		try {
			serverPort = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Usage: java -jar Server.jar <serverPort>");
			System.exit(0);
		}

		try {
			Mediator mediator = new Mediator();
			Registry registry = LocateRegistry.createRegistry(serverPort);
			registry.bind(IMediator.REMOTE_OBJECT_NAME, mediator);
			System.out.format("Shared white board server ready on port %d%n", serverPort);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					registry.unbind(IMediator.REMOTE_OBJECT_NAME);
					JSONObject data = new JSONObject();
					data.put("actionType", ActionType.SERVER_SHUTDOWN.toString());
					mediator.broadcast(data.toString(), -1);
				} catch (RemoteException | NotBoundException e) {
					// Do nothing
				}
			}));
		} catch (RemoteException e) {
			System.out.println("Cannot bind the remote object");
			System.exit(0);
		} catch (AlreadyBoundException e) {
			System.out.println("The remote object already bound");
			System.exit(0);
		}
	}
}
