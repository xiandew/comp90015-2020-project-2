package remote;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICollaborator extends Remote {
	public int getId() throws RemoteException;

	public String getUsername() throws RemoteException;

	public boolean getIsJoined() throws RemoteException;

	// Connect to a mediator
	public void connect(String serverAddress, int serverPort) throws RemoteException, NotBoundException;

	public void updateBoard() throws IOException, RemoteException;

	// Disconnect from the mediator
	public void unregister() throws RemoteException;

	// Outgoing messages/data
	public void send(String data, int to) throws RemoteException;

	public void broadcast(String data) throws RemoteException;

	// Incoming messages/data
	public void notify(String data, int from) throws RemoteException;
}
