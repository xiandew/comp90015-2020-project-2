package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface IMediator extends Remote {
	public static final String REMOTE_OBJECT_NAME = "SharedWhiteBord";

	public int register(ICollaborator c) throws RemoteException;

	public int registerAsManager(ICollaborator m) throws RemoteException;

	public ConcurrentLinkedQueue<String> getExecutedBoardActions() throws RemoteException;

	public void removeUser(int id) throws RemoteException;
	
	public void removeManager() throws RemoteException;
	
	public int getManagerId() throws RemoteException;

	public LinkedList<String> getUsers() throws RemoteException;

	public void send(String data, int to, int from) throws RemoteException;

	public void broadcast(String data, int from) throws RemoteException;

	public void addBoardActions(String data) throws RemoteException;
	
	public void resetBoardActions() throws RemoteException;

	public void resetBoardActions(String boardActions) throws RemoteException;
}
