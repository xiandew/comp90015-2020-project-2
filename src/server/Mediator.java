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
import utils.ActionType;

public class Mediator extends UnicastRemoteObject implements IMediator {
	private static final long serialVersionUID = 1L;
	private ICollaborator manager;
	private HashMap<Integer, ICollaborator> users = new HashMap<>();
	private int nUsers = 0;
	private LinkedList<String> executedBoardActions = new LinkedList<>();

	protected Mediator() throws RemoteException {
		super();
	}

	@Override
	public int register(ICollaborator c) throws RemoteException {
		if (this.manager == null) {
			return -1;
		}
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
	public boolean removeUser(int id) throws RemoteException {
		if (this.users.remove(id) == null) {
			return false;
		}
		if (manager != null && id == manager.getId()) {
			manager = null;
			// Broadcast messages
			JSONObject data = new JSONObject();
			data.put("actionType", ActionType.MANAGER_EXIT.toString());
			this.resetBoardActions();
			this.broadcast(data.toString(), id);
		}
		return true;
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
		for (ICollaborator user : this.users.values()) {
			try {
				user.notify(data, from);
			} catch (RemoteException e) {
				// The user might be disconnected
				this.removeUser(user.getId());
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
