package server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;

import remote.ICollaborator;
import remote.IMediator;

public class Mediator extends UnicastRemoteObject implements IMediator {
	private static final long serialVersionUID = 1L;
	private HashMap<UUID, ICollaborator> users;

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

	protected Mediator() throws RemoteException {
		this.users = new HashMap<>();
	}

	@Override
	public UUID register(ICollaborator c) throws RemoteException {
		UUID id = UUID.randomUUID();
		this.users.put(id, c);
		return id;
	}

	@Override
	public boolean removeCollaborator(UUID id) throws RemoteException {
		if (this.users.remove(id) == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean send(String data, UUID to, UUID from) throws RemoteException {
		return this.users.get(to).notify(data, from);
	}

	@Override
	public boolean broadcast(String data, UUID from) throws RemoteException {
		for (ICollaborator user : this.users.values()) {
			if (!user.notify(data, from)) {
				return false;
			}
		}
		return true;
	}
}
