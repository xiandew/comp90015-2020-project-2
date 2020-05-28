package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface IMediator extends Remote {
	public static final String REMOTE_OBJECT_NAME = "SharedWhiteBord";

	public UUID register(ICollaborator c) throws RemoteException;

	public boolean removeCollaborator(UUID id) throws RemoteException;

	public boolean send(String data, UUID to, UUID from) throws RemoteException;

	public boolean broadcast(String data, UUID from) throws RemoteException;
}
