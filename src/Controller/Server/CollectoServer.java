package Controller.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Controller.Exceptions.exitProgram;
import utils.TextIO;

public class CollectoServer implements Runnable {
	
	private ServerSocket ss;
	private String srvname = "CollectoServer";
	
	// Next client number, for every new connection
	private int nextclientnb;
	
	// View of collecto sever
	public CollectoServerTUI view;
	
	//list of connected clienthandlers
	ArrayList<CollectoClientHandler> clients = new ArrayList<CollectoClientHandler>();
	
	// List of client's usernames connected to server
	private ArrayList<String> usernames;
	
	// List of clienthandlers in queue that want to play
	public ArrayList<CollectoClientHandler> queue;
	int port = 0;
	
	/**
	 * Constructor that takes name of user as parameter.
	 * name is requested in main method
	 */
	public CollectoServer(String yourname) {
		nextclientnb = 0;
		usernames = new ArrayList<String>();
		queue = new ArrayList<CollectoClientHandler>();				
		view = new CollectoServerTUI();	
		this.srvname += " by " + yourname;
	}
	// same constructor but takes port as 2nd param. To facilitate testing
	public CollectoServer(String yourname, int port) {
		nextclientnb = 0;
		this.port = 4114;
		usernames = new ArrayList<String>();
		queue = new ArrayList<CollectoClientHandler>();				
		view = new CollectoServerTUI();	
		this.srvname += " by " + yourname;
	}
	
	/**
	 * boolean method called in CollectoClient.
	 * tries to add username to usernames list of server
	 * if username already in list then return true
	 * else return false
	 * 
	 * @param username
	 * @return
	 */
	public boolean addUsername(String username) {
		if (!getUsernames().contains(username)) {
			getUsernames().add(username);
			view.showMessage(username + " has logged in");
			return true;
		}
		return false;
	}
	
	/**
	 * Sets up the server, starts queuechecker thread
	 * And continuously listens for new clients.
	 */
	public void start() {
		boolean openNewSocket = true;
		while (openNewSocket) {
			try {
				// Sets up the server
				setupServer();
				// start queuechecker thread
				QueueChecker queuechecker = 
						new QueueChecker(this);
				new Thread(queuechecker).start();
				
				while (true) {
					Socket sock = ss.accept();
					String name = "Client " 
							+ String.format("%02d", nextclientnb++);
					view.showMessage("New client [" + name + "] connected!");
					CollectoClientHandler handler = 
							new CollectoClientHandler(sock, this, name);
					new Thread(handler).start();
					getClients().add(handler);
					
				}

			} catch (exitProgram e1) {
				// If setup() throws an ExitProgram exception, 
				// stop the program.
				openNewSocket = false;
			} catch (IOException e) {
				System.out.println("A server IO error occurred: " 
						+ e.getMessage());

				if (!view.getBoolean("Do you want to open a new socket?")) {
					openNewSocket = false;
				}
			}
		}
		view.showMessage("See you later!");
	}
	
	
	public void setupServer() throws exitProgram {
		ss = null;
		while (ss == null) {
			if (port == 0) {
				port = view.getInt("Please enter the server port.");				
			}

			// try to open a new ServerSocket
			try {
				view.showMessage("Attempting to open a socket at 127.0.0.1 "
						+ "on port " + port + "...");
				ss = new ServerSocket(port, 0, 
						InetAddress.getByName("127.0.0.1"));
				view.showMessage("Server started at port " + port);
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on "
						+ "127.0.0.1" + " and port " + port + ".");

				if (!view.getBoolean("Do you want to try again?")) {
					throw new exitProgram("User indicated to exit the "
							+ "program.");
				}
			}
		}
	}

	/**
	 * Removes client's username from username list.
	 * @param client
	 */
	public void removeClient(CollectoClientHandler client) {
		this.usernames.remove(client.getUsername());
		this.clients.remove(client);
	}
	
	/**
	 * Used in clienthandler sendHello to add servername to Handshake.
	 * @return server's name
	 */
	public String getServerName() {
		return this.srvname;
	}
	
	
	/**
	 * Called by clientHandler.
	 * @return list of usernames of clients connected
	 */
	public String getList() {
		String result = "LIST";
		for (String username : this.getUsernames()) {
			result += "~" + username;
		}
		return result;
	}
	
	public ArrayList<String> getUsernames() {
		return usernames;
	}
	
	/**
	 * @return the clients
	 */
	public ArrayList<CollectoClientHandler> getClients() {
		return clients;
	}
	
	/**
     * Main asking for name of user, then creating server with that name as param
     * And in the end starting the server.
     */
	public static void main(String[] args) {
		System.out.println("What's your name ?");
		String name = null;
		while (name == null) {				
			name = TextIO.getlnString();
		}
		CollectoServer server = new CollectoServer(name);
		server.view.showMessage("Welcome to the " + server.getServerName() + " ! Starting...");
		server.start();
	}
	
	@Override
	//Runnable to start server as thread when testing, otherwise not necessary
	public void run() {
		this.start();
	}
}
