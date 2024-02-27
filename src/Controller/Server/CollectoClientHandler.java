package Controller.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import Controller.Exceptions.serverUnavailableException;
import Model.Board.Game;


public class CollectoClientHandler implements Runnable {

	private BufferedReader in;
	private BufferedWriter out;
	private Socket sock;
	
	// Game variable, null if not in game
	private Game game;
	
	// Variable username set after succesfull login
	private String username = "";
	private boolean loggedin;
	
	boolean run;
	private CollectoServer srv;
	private String name;
	
	/**
	 * Constructor for CollectoClientHandler.
	 * @requires sock, server, and name to not be null
	 * 
	 * @param sock
	 * @param srv
	 * @param name
	 */
	public CollectoClientHandler(Socket sock, CollectoServer srv, String name) {
		try {
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(
					new OutputStreamWriter(sock.getOutputStream()));
			this.sock = sock;
			this.srv = srv;
			this.name = name;
			this.game = null;
		} catch (IOException e) {
			shutdown();
		}
	}
	
	/**
	 * When Game starts it calls setgame(this)
	 * to set this client's game to that Game instance.
	 * When game is over Game class sets this game to null
	 * 
	 * @param game
	 */
	public void setGame(Game game) {
		this.game = game;
	}
	
	/**
	 * Does HELLO handshake with client.
	 * Then waits for succesful login.
	 * And then starts loop to handle client messages until disconnection
	 */
	public void run() {
		String msg;
		run = true;
		while (run) {			
			try {
				boolean hello = checkHello();
				if (!hello) {
					shutdown();
					return;
				}
				this.handleLogin();
				if (loggedin) {
					msg = in.readLine();
					while (msg != null) {
						srv.view.showMessage("> [" + name + "] Incoming: " + msg);
						handleCommand(msg);
						msg = in.readLine();
					}
					shutdown();
				}
			} catch (IOException | serverUnavailableException e) {
				shutdown();
			}
		}
	}
	
	
	/**
	 * Called after Handshake made and Login succesfull.
	 * Handles messages from clients
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws serverUnavailableException
	 */
	private synchronized void handleCommand(String msg)
			throws IOException, serverUnavailableException {		
		String command = msg.split("~")[0];
		switch (command) {
			case "HELLO": 
				this.sendError("Handshake already made");
				break;
				
			case "LOGIN": 
				this.sendError("You are already logged in with username: " + this.username);
				break;
		
			case "QUEUE":
				if (srv.queue.contains(this)) {
					this.sendError("Already in queue");
				} else {
					srv.queue.add(this);
				}
				break;
				
			case "LIST":
				this.sendMessage(srv.getList());
				break;
				
			case "MOVE":
				this.handleMove(msg);
				break;
		
		
			default: this.sendError("Command not recognized");				
		}
	}
	
	
	/**
	 * Is called after Handshake completed.
	 * 
	 * Login succeful if username sent by client is not already taken
	 * called continuously until login succefull
	 * 
	 */
	private void handleLogin() {
		try {
			while (!loggedin) {
				String msg = in.readLine();
				String[] msgsplit = msg.split("~");
				if (!msgsplit[0].equals("LOGIN")) {
					this.sendError("Please login first");
					return;
				}
				if (msgsplit.length != 2) {
					this.sendError("Invalid format");
					return;
				}
				String usernametry = msgsplit[1];
				loggedin = srv.addUsername(usernametry);
				if (loggedin) {
					this.sendMessage("LOGIN");
					this.username = usernametry;
				} else {
					this.sendMessage("ALREADYLOGGEDIN");
				}
			}
		} catch (IOException e) {
			srv.view.showMessage("No Login received from " + this.name);
			this.shutdown();
		}
	}
	
	
	/**
	 * Method to check if hello message is received from Client
	 * Is called each time a ClientHandler is created.
	 * 
	 * @return true if HELLO message received from client else return false
	 */
	private boolean checkHello() {
		try {
			String msg = in.readLine();
			String[] msgsplit = msg.split("~");
			if (msgsplit[0].equals("HELLO")) {
				sendHello();
			}
			return true;
		} catch (serverUnavailableException e) {
			srv.view.showMessage("Hello could not be sent");
			return false;
		} catch (IOException e) {
			srv.view.showMessage("No HELLO received from client");
			return false;
		}
	}
	
	
	/**
	 * Method that handles move when "MOVE" message received from client
	 * if message follows protocol move is sent to game in which client is in.
	 * if client not in a game then error sent to this client.
	 * 
	 */
	private void handleMove(String movecommand) {
		ArrayList<Integer> movelist = new ArrayList<Integer>();
		String[] cmdsplit = movecommand.split("~");
		if (this.game == null) {
			this.sendError("You are not in game, queue up first");
		}
		try {
			if (cmdsplit.length == 2) {
				int move = Integer.parseInt(cmdsplit[1]);
				movelist.add(move);
				game.getMove(this, movelist);
			} else if (cmdsplit.length == 3) {
				int move1 = Integer.parseInt(cmdsplit[1]);
				int move2 = Integer.parseInt(cmdsplit[2]);
				movelist.add(move1);
				movelist.add(move2);
				game.getMove(this, movelist);
			} else {
				this.sendError("Invalid format");
			}
			
		} catch (NumberFormatException e) {
			this.sendError("Invalid format");
		}
	}
	
	
	/**
	 * Method called in handle hello that actually sends the HELLO message to client.
	 * @throws serverUnavailableException
	 */
	public void sendHello() throws serverUnavailableException {
		if (out != null) {
			try {
				out.write("HELLO~" + srv.getServerName());
				out.newLine();
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Method to sendMessage to this client.
	 * 
	 * @param msg
	 */
	public synchronized void sendMessage(String msg) {
		if (out != null) {
			try {
				out.write(msg);
				System.out.println("Message sent to" + username + ": " + msg);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				srv.view.showMessage("Message not sent, " + username + " disconnected");
				this.shutdown();
			}
		}
	}
	
	
	/**
	 * Method to facilitate sending errors to client
	 * Takes error msg you want to send to client as parameter and sends message to client.
	 * 
	 * @param msg
	 */
	public void sendError(String msg) {
		sendMessage("ERROR~" + msg);
	}
	
	
	/**
	 * Used in server to remove username from list when client disconnects.
	 * @return username of client
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Method called when no more messages are being received from client.
	 * disconnects from game if client in game
	 */
	private void shutdown() {
		srv.view.showMessage("> [" + name + "] Shutting down.");
		if (game != null) {
			game.disconnect(this);
		}
		try {
			in.close();
			out.close();
			sock.close();
		} catch (IOException e) {
			srv.view.showMessage("Could not close connection with client");
		}
		srv.removeClient(this);
		run = false;
	}

}
