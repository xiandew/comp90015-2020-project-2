package remote;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ICollaborator extends Remote {
	public UUID getId() throws RemoteException;

	// Connect to a mediator
	public void connect(String serverAddress, int serverPort) throws RemoteException, NotBoundException;
	
	// Disconnect from the mediator
	public void unregister() throws RemoteException;

	// Outgoing messages/data
	public boolean send(String data, UUID to) throws RemoteException;

	public boolean broadcast(String data) throws RemoteException;

	// Incoming messages/data
	public boolean notify(String data, UUID from) throws RemoteException;
}
