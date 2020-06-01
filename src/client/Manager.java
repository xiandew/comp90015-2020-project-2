package client;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import remote.IMediator;
import utils.ActionType;

public class Manager extends User {
	private static final long serialVersionUID = 1L;
	private String savedBoardFilePath;

	public Manager(String username, String serverAddress, int serverPort) throws RemoteException {
		super(username, serverAddress, serverPort);
		this.gui.addManagerControls();
		this.updateBoard();
		this.updateUserList();
		this.broadcastUserListUpdate();
		this.launchGUI();
	}

	@Override
	public boolean getIsJoined() throws RemoteException {
		return true;
	}

	@Override
	public void connect(String serverAddress, int serverPort) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(serverAddress, serverPort);
		this.mediator = (IMediator) registry.lookup(IMediator.REMOTE_OBJECT_NAME);
		this.id = this.mediator.registerAsManager(this);
		if (this.id < 0) {
			System.out.println("The board has been created. Please join instead");
			System.exit(0);
		}
	}

	@Override
	public void unregister() throws RemoteException {
		try {
			this.mediator.removeManager();
			this.mediator.resetBoardActions();
			// Broadcast messages
			JSONObject data = new JSONObject();
			data.put("actionType", ActionType.MANAGER_EXIT.toString());
			this.broadcast(data.toString());
		} catch (Exception e) {
			// Do nothing
		}
	}

	@Override
	public void updateUserList() throws RemoteException {
		this.gui.updateUserList(this.getUserList(), true);
	}

	@Override
	public void launchGUI() {
		this.gui.mntmNew.addActionListener((ActionEvent ev) -> {
			this.refreshBoard();

			// Broadcast messages
			JSONObject data = new JSONObject();
			data.put("actionType", ActionType.FILE_NEW.toString());
			try {
				this.mediator.resetBoardActions();
				this.broadcast(data.toString());
			} catch (RemoteException e) {
				// Do nothing
			}
		});

		this.gui.mntmOpen.addActionListener((ActionEvent ev) -> {
			if (this.gui.fileChooser.showOpenDialog(this.gui.frmMain) == JFileChooser.APPROVE_OPTION) {
				File savedBoardFile = this.gui.fileChooser.getSelectedFile();
				savedBoardFilePath = savedBoardFile.getAbsolutePath();

				try {
					JSONTokener jt = new JSONTokener(new FileReader(savedBoardFilePath));
					if (!jt.more()) {
						return;
					}
					JSONArray boardActions = new JSONArray(jt);

					this.mediator.resetBoardActions(boardActions.toString());
					this.refreshBoard(boardActions);

					// Broadcast messages
					JSONObject data = new JSONObject();
					data.put("actionType", ActionType.FILE_OPEN);
					data.put("boardActions", boardActions.toString());
					this.broadcast(data.toString());

				} catch (NullPointerException e) {
					this.gui.showMessageDialog("Please select a file to open");
				} catch (JSONException e) {
					this.gui.showMessageDialog("Unable to parse the selected file");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		this.gui.mntmSave.addActionListener((ActionEvent ev) -> {
			this.save();
		});

		this.gui.mntmSaveAs.addActionListener((ActionEvent ev) -> {
			this.saveAs();
		});

		this.gui.tableUsers.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				int row = gui.tableUsers.rowAtPoint(ev.getPoint());
				int col = gui.tableUsers.columnAtPoint(ev.getPoint());
				int idCol = gui.tableUsers.getColumn("ID").getModelIndex();
				int actionCol = gui.tableUsers.getColumn("Action").getModelIndex();
				if (row >= 0 && col == actionCol && gui.tableUsers.getValueAt(row, col) == "Kick Out") {
					int id = (int) gui.tableUsers.getValueAt(row, idCol);
					String msg = String.format("You are going to kick out user %s", id);
					if (JOptionPane.YES_OPTION == gui.showConfirmDialog(msg)) {
						gui.modelUsers.removeRow(gui.tableUsers.convertRowIndexToModel(row));

						// Use thread to prevent the blocking when sending messages
						new Thread(() -> {
							// Broadcast messages
							JSONObject data = new JSONObject();
							try {
								data.put("actionType", ActionType.KICKED_OUT);
								mediator.send(data.toString(), id, Manager.this.id);
								mediator.removeUser(id);
								data.put("actionType", ActionType.USER_LIST_UPDATE);
								broadcast(data.toString());
							} catch (Exception e) {
								// System.out.println("Unable to send messages");
							}
						}).start();
					}
				}
			}
		});

		this.gui.tableNotifications.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				int row = gui.tableNotifications.rowAtPoint(ev.getPoint());
				int col = gui.tableNotifications.columnAtPoint(ev.getPoint());
				int idCol = gui.tableNotifications.getColumn("UserID").getModelIndex();
				int actionCol = gui.tableNotifications.getColumn("Action").getModelIndex();
				if (row >= 0 && col == actionCol && gui.tableNotifications.getValueAt(row, col) == "Approve") {
					int id = (int) gui.tableNotifications.getValueAt(row, idCol);

					String msg = String.format("Approve user %s to join?", id);
					int option = gui.showConfirmDialog(msg);

					if (option == JOptionPane.YES_OPTION) {
						gui.modelNotifications.setValueAt("Approved", row, col);
					}
					if (option == JOptionPane.NO_OPTION) {
						gui.modelNotifications.setValueAt("Declined", row, col);
					}

					new Thread(() -> {
						JSONObject data = new JSONObject();
						try {
							if (option == JOptionPane.YES_OPTION) {
								data.put("actionType", ActionType.JOIN_REQUEST_APPROVED);
								send(data.toString(), id);
								data.put("actionType", ActionType.USER_LIST_UPDATE);
								mediator.broadcast(data.toString(), id);
							}
							if (option == JOptionPane.NO_OPTION) {
								data.put("actionType", ActionType.JOIN_REQUEST_DECLINED);
								send(data.toString(), id);
							}
						} catch (Exception e) {
							gui.modelNotifications.setValueAt("N/A", row, col);
							gui.showMessageDialog("Unable to response the request. The user might have exited");
						}
					}).start();
				}
			}
		});

		this.gui.launch();
	}

	@Override
	public void notify(String data, int from) throws RemoteException {
		super.notify(data, from);
		if (from == this.id) {
			return;
		}

		JSONObject jData = new JSONObject(data);
		ActionType actionType = ActionType.valueOf((String) jData.get("actionType"));
		switch (actionType) {
		case JOIN_REQUEST:
			String msgFmt = "User [%s] request to join the board";
			String msg = String.format(msgFmt, jData.get("username"));
			this.gui.modelNotifications.addRow(new Object[] { jData.get("id"), msg, "Approve" });
			break;
		default:
			break;
		}
	}

	private void save() {
		if (savedBoardFilePath != null) {
			try (PrintWriter printWriter = new PrintWriter(savedBoardFilePath, "UTF-8");) {
				JSONArray jExecutedBoardActions = new JSONArray();
				for (String boardAction : this.mediator.getExecutedBoardActions()) {
					jExecutedBoardActions.put(boardAction);
				}
				printWriter.println(jExecutedBoardActions.toString());
			} catch (Exception e) {
				// Do nothing
			}
		} else {
			this.saveAs();
		}
	}

	private void saveAs() {
		if (this.gui.fileChooser.showSaveDialog(this.gui.frmMain) == JFileChooser.APPROVE_OPTION) {
			File savedBoardFile = this.gui.fileChooser.getSelectedFile();
			savedBoardFilePath = savedBoardFile.getAbsolutePath();
			this.save();
		}
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
			System.out.println("Usage: java -jar CreateWhiteBoard.jar <serverAddress> <serverPort> username");
			System.exit(0);
		}

		try {
			new Manager(username, serverAddress, serverPort);
		} catch (Exception e) {
			System.out.println("Cannot connect to the server");
			System.exit(0);
		}
	}
}
