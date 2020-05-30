package server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import remote.ICollaborator;
import remote.IMediator;

public class Mediator extends UnicastRemoteObject implements IMediator {
	private static final long serialVersionUID = 1L;

	private LinkedList<String> executedBoardActions = new LinkedList<>();
	private HashMap<Integer, ICollaborator> users = new HashMap<>();
	private ICollaborator manager;
	private int nUsers = 0;

	protected Mediator() throws RemoteException {
		super();
	}

	@Override
	public synchronized int register(ICollaborator c) throws RemoteException {
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
	public LinkedList<String> getExecutedBoardActions() throws RemoteException {
		return this.executedBoardActions;
	}

	@Override
	public synchronized void removeUser(int id) throws RemoteException {
		this.users.remove(id);
	}

	@Override
	public synchronized void removeManager() throws RemoteException {
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
	public synchronized void broadcast(String data, int from) throws RemoteException {
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
	public synchronized void addBoardActions(String data) throws RemoteException {
		this.executedBoardActions.add(data);
	}

	@Override
	public synchronized void resetBoardActions() throws RemoteException {
		this.executedBoardActions.clear();
	}

	@Override
	public synchronized void resetBoardActions(String boardActions) throws RemoteException {
		this.resetBoardActions();
		for (Object jBoardAction : new JSONArray(boardActions)) {
			this.executedBoardActions.add(jBoardAction.toString());
		}
	}

	public static void main(String[] args) {

		try {
			Mediator mediator = new Mediator();
			Registry registry = LocateRegistry.getRegistry();
			registry.bind(IMediator.REMOTE_OBJECT_NAME, mediator);
			System.out.println("Shared White Board Ready");

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					registry.unbind(IMediator.REMOTE_OBJECT_NAME);
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
