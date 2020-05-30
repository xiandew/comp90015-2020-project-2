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
import javax.swing.table.DefaultTableModel;

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
	}

	public void launchGUI() {
		this.gui.mntmNew.addActionListener((ActionEvent ev) -> {
			this.refreshBoard();

			// Broadcast messages
			JSONObject data = new JSONObject();
			data.put("actionType", ActionType.FILE_NEW.toString());
			try {
				this.mediator.resetBoardActions();
				this.mediator.broadcast(data.toString(), this.id);
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
					this.mediator.broadcast(data.toString(), this.id);

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

		this.gui.launchAsManager();
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
	public void updateUserList() throws RemoteException {
		this.gui.updateUserList(this.getUserList(), true);
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
					if (JOptionPane.OK_OPTION == gui.showConfirmDialog(msg)) {
						DefaultTableModel model = (DefaultTableModel) gui.tableUsers.getModel();
						model.removeRow(gui.tableUsers.convertRowIndexToModel(row));

						// Use thread to prevent the blocking when sending messages
						new Thread(() -> {
							// Broadcast messages
							JSONObject data = new JSONObject();
							try {
								data.put("actionType", ActionType.KICKED_OUT);
								mediator.send(data.toString(), id, Manager.this.id);
								mediator.removeUser(id);
								data.put("actionType", ActionType.USER_LIST_UPDATE);
								mediator.broadcast(data.toString(), Manager.this.id);
							} catch (Exception e) {
								// System.out.println("Unable to send messages");
							}
						}).start();
					}
				}
			}
		});
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
		} catch (RemoteException e) {
			System.out.println("Cannot connect to the server");
			System.exit(0);
		}
	}
}
