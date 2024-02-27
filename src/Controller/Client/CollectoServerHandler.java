package Controller.Client;

import Controller.Exceptions.serverUnavailableException;

public class CollectoServerHandler implements Runnable {
	CollectoClient client;
	
	/**
	 * Constructor of server handler taking client as parameter.
	 * @param client
	 */
	public CollectoServerHandler(CollectoClient client) {
		this.client = client;
	}
	
	
	/**
	 * Thread loop that listens for input from server
	 * And sends it to handleServerMessage in client.
	 */
	public void run() {
		boolean run = true;
		while (run) {
			String svrmsg;
			try {
				svrmsg = client.readLineFromServer();
				client.view.showMessage("Message from server: " + svrmsg);
				client.handleServerMessage(svrmsg);
			} catch (serverUnavailableException e) {
				client.view.showMessage("Disconnected from server");
				client.closeConnection();
				System.exit(0);
			}
		}
	}
	
}
