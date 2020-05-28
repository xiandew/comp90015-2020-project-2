package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import client.GUI.GUI;
import remote.ICollaborator;
import remote.IMediator;

public class Collaborator extends UnicastRemoteObject implements ICollaborator {
	private static final long serialVersionUID = 1L;

	protected IMediator mediator;

	private UUID id;
	private String username;

	public static void main(String[] args) {
		String serverAddress = null;
		Integer serverPort = null;
		String username = null;

		try {
			serverAddress = args[0];
			serverPort = Integer.parseInt(args[1]);
			username = args[2];

		} catch (Exception e) {
			System.out.println("Usage: java JoinWhiteBoard <serverAddress> <serverPort> username");
			System.exit(0);
		}

		Collaborator collaborator;
		try {
			collaborator = new Collaborator(username);
			collaborator.connect(serverAddress, serverPort);
			System.out.println("Successfully joined the white board");

			GUI gui = new GUI(collaborator);
			gui.launch();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					collaborator.unregister();
				} catch (RemoteException e) {
					// Do nothing
				}
			}));
		} catch (NotBoundException e) {
			System.out.println("The remote object not bound");
		} catch (RemoteException e) {
			System.out.println("Cannot connect to the server");
			System.exit(0);
		}
	}

	protected Collaborator(String username) throws RemoteException {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	@Override
	public UUID getId() {
		return this.id;
	}

	@Override
	public void connect(String serverAddress, int serverPort) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(serverAddress, serverPort);
		this.mediator = (IMediator) registry.lookup(IMediator.REMOTE_OBJECT_NAME);
		this.id = this.mediator.register(this);
	}

	@Override
	public void unregister() throws RemoteException {
		this.mediator.removeCollaborator(this.id);
	}

	@Override
	public boolean send(String data, UUID to) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean broadcast(String data) throws RemoteException {
		return this.mediator.broadcast(data, this.id);
	}

	@Override
	public boolean notify(String data, UUID from) throws RemoteException {
		if (from == this.id) {
			return true;
		}
		// TODO
		return true;
	}
}
