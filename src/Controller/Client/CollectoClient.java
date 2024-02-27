package Controller.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import Controller.Exceptions.exitProgram;
import Controller.Exceptions.protocolExceptions;
import Controller.Exceptions.serverUnavailableException;
import utils.TextIO;


public class CollectoClient implements Runnable {
	
	private Socket serverSock;
	private BufferedReader in;
	private BufferedWriter out;
	public CollectoClientTUI view;
	String username;
	String opname;
	
	// boolean value made to keep track who's turn it is
	boolean mymove;
	
	ArrayList<Integer> myballs;
	ArrayList<Integer> opballs;
	private boolean connected;	
	
	// default IP adress and port, the ones from UT reference server
	public String serverIP = "130.89.253.65";
	public int serverPort = 4114;
	
	
	/**
	 * Constructor that takes serverIP and port as parameters.
	 * Both are setup in main method
	 */
	public CollectoClient(String serverIP, int port) {
		view = new CollectoClientTUI(this);
		this.serverIP = serverIP;
		this.serverPort = port;
	}
	
	
	/**
	 * Constructor that takes no parameters.
	 * Initialises client and view without custom ip and port, thus keeping UT server address
	 */
	public CollectoClient() {
		view = new CollectoClientTUI(this);
	}
	
	// 
	/**
	 * Start the CollectoClient, create a connection.
	 * Once HELLO handshake handled
	 * New thread serverhandler(handle server input) is started
	 * and clientTUI is started(handle user input)
	 * 
	 */
	public void start() {
		connected = true;
		while (connected) {
			try {
				createConnection();
				try {
					try {
						handleHello();
					} catch (protocolExceptions e) {
						view.showMessage(e.getMessage());
						view.showMessage("Protocol not followed");
						closeConnection();
						continue;
					}
					//display commands and send string ask for username
					view.printMenu();
					view.showMessage("Please login");
					
					//start serverHandler thread to continuously listen to server messages
					CollectoServerHandler serverhandler = new CollectoServerHandler(this);  
					new Thread(serverhandler).start();
					
					//start TUI loop to handler client input
					view.start();			
					
				} catch (serverUnavailableException e) {
					view.showMessage("Error occured");
					closeConnection();
				}
				connected = view.getBoolean("Connect to a new server?");
			} catch (exitProgram e1) {
				connected = false;
			}
			view.showMessage("See you!");
		}
	}	

	public void createConnection() throws exitProgram {
		clearConnection();
		while (serverSock == null) {
			String host = serverIP;
			int port = serverPort;

			// try to open a Socket to the server
			try {
				InetAddress addr = InetAddress.getByName(host);
				view.showMessage("Attempting to connect to " + addr + ":" 
					+ port + "...");
				serverSock = new Socket(addr, port);
				view.showMessage("Connected to server");
				in = new BufferedReader(new InputStreamReader(
						serverSock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(
						serverSock.getOutputStream()));
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on " 
					+ host + " and port " + port + ".");

				//Do you want to try again? (ask user, to be implemented)
				if (!view.getBoolean("Do you want to try again?")) {
					throw new exitProgram("User indicated to exit.");
				}
			}
		}
	}
	
	public void clearConnection() {
		serverSock = null;
		in = null;
		out = null;
	}
	
	public synchronized void sendMessage(String msg) 
			throws serverUnavailableException {
		if (out != null) {
			try {
				out.write(msg);
				view.showMessage("Message sent: " + msg);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				throw new serverUnavailableException("Could not write "
						+ "to server.");
			}
		} else {
			throw new serverUnavailableException("Could not write "
					+ "to server.");
		}
	}
	
	public String readLineFromServer() 
			throws serverUnavailableException {
		if (in != null) {
			try {
				// Read and return answer from Server
				String answer = in.readLine();
				if (answer == null) {
					throw new serverUnavailableException("Could not read "
							+ "from server.");
				}
				return answer;
			} catch (IOException e) {
				throw new serverUnavailableException("Could not read "
						+ "from server.");
			}
		} else {
			throw new serverUnavailableException("Could not read "
					+ "from server.");
		}
	}
	

	public void closeConnection() {
		System.out.println("Closing the connection...");
		try {
			in.close();
			out.close();
			serverSock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleHello() 
			throws serverUnavailableException, protocolExceptions {
		sendMessage("HELLO~Client by TIMOTHY");
		String svrmsg = readLineFromServer();
		String[] msgsplit = svrmsg.split("~");
		if (msgsplit[0].equals("HELLO")) {
			view.showMessage(svrmsg);
			view.showMessage("handleHello handshake finished");
		}
	}
	
	
	
	/**
	 * Method called by serverhandler thread when message received from server
	 * Handles message.
	 * @param svrmsg
	 */
	public void handleServerMessage(String svrmsg) {
		String[] msgsplit = svrmsg.split("~");
		String command = msgsplit[0];
		//implement switch case
		
		switch (command) {
		
			case "HELLO" : 
				view.showMessage("Handshake already finished");
				break;
		
			case "LOGIN" : 
				view.showMessage("\nLogin succesfull, "
						+ "type in 'queue' when your ready to play a game");	
				break;
			
			case "ALREADYLOGGEDIN":
				view.showMessage("Username already used, try login again");	
				break;
			
			case "ERROR":
				view.showMessage("Error received from server: " + msgsplit[1]);
				break;
				
			case "LIST":
				for (String name : msgsplit) {
					view.showMessage(name);
				}
				break;
				
			case "NEWGAME" :
				ArrayList<Integer> fields = new ArrayList<Integer>();
				if (msgsplit.length != 52) {
					view.showMessage("NEWGAME command not in correct format");
					return;
				}
				for (int index = 1; index <= 49; index++) {
					fields.add(Integer.parseInt(msgsplit[index]));
				}
				view.setupBoard(fields);
				myballs = new ArrayList<Integer>();
				opballs = new ArrayList<Integer>();
				
				//if my name is first then I start and mymove set to true
				if (msgsplit[50].equals(this.username)) {
					opname = msgsplit[51];
					view.showMessage("Your playing against " + opname);
					this.determineMove();
					mymove = true;
				} else {
					opname = msgsplit[50];
					view.showMessage("Your playing against " + opname + "\n" + "They start");
					mymove = false;
				}
				view.displayBoard();
				break;
				
				
			case "MOVE" :
				int move1 = Integer.parseInt(msgsplit[1]);
				if (msgsplit.length == 3) {
					int move2 = Integer.parseInt(msgsplit[2]);
					view.clientboard.tryMove(move1, move2);
				} else {
					view.clientboard.tryMove(move1);
				}
				Collection<Integer> ballscollected = view.clientboard.collectBalls();
				if (mymove) {
					view.showMessage("Balls you collected: " + ballscollected);
					myballs.addAll(ballscollected);
				} else {
					view.showMessage("Balls collected by " + opname + ": " + ballscollected);
					opballs.addAll(ballscollected);
					if (!view.clientboard.gameOver()) {
						this.determineMove();						
					}
				}
				view.displayBoard();
				mymove = mymove ? false : true;
				break;
			
				
			case "GAMEOVER":
				String result = "";
				result += "Game is over";
				if (msgsplit[1].equals("DRAW")) {
					result += ", ended in a draw\n"
							+ "You and '" + opname + "' both had a score of " 
							+ getPoints(myballs) + "\n";
				} else if (msgsplit[1].equals("DISCONNECT")) {
					result += ", opponent disconnected\n";
				}
				result += "\nThese are the balls you collected: " + myballs;
				result += "\nThese are the balls '" + opname + "' collected: " + opballs;
				if (msgsplit.length == 3) {	
					if (msgsplit[2].equals(username)) {
						view.showMessage("You won with a final score of "
								+ getPoints(myballs) + " points to " + getPoints(opballs));
					} else {
						view.showMessage("You lost with a final score of "
								+ getPoints(myballs) + " points to " + getPoints(opballs)
								+ "\n Mission failed we'll get em next time");
					}
				}
				result += "\n Type in 'queue' if you want to play again";
				
				view.showMessage(result);
				break;
			
			default: view.showMessage("Command from server not recognized, try help");
		}
		
	}
		
	/**
	 * DetermineMove called in Client when its our turn to make move.
	 * In normal CollectoClient only prints a string asking for move
	 * Made in order to facilitate inheritance in CollectoComputerClient
	 * 
	 * @return null
	 */
	public void determineMove() {
		view.showMessage("Your turn, make a move " + username);
	}
	
	
	/*
	 * Static method to get points out of ball list
	 * public for it to be used in Strategy
	 * 
	 * @requires list collected to only contain int between 1 and 6
	 */
    public static int getPoints(Collection<Integer> collected) {
    	int score = 0;
    	for (int i = 1; i <= 6; i++) {
    		int occurrences = Collections.frequency(collected, i);
    		if (occurrences != 0) {
    			score += Math.floorDiv(occurrences, 3);    		
    		}
    	}
    	return score;
    }
	
    /**
     * Main asking for ip:port if user does not want to play on UT server.
     * Creates and launches CollectoClient accordingly
     */
	public static void main(String[] args) {
		System.out.println("Do you want to play on UT reference server ?");
		if (!TextIO.getBoolean()) {
			System.out.println("Please type in ipadress:port");
			String adress = "";
			while (adress.equals("")) {				
				adress = TextIO.getlnString();
			}
			String[] split = adress.split(":");
			if (split.length != 2) {
				System.out.println("Uncorrect format, joining UT reference server");
				(new CollectoClient()).start();
			} else {
				String serverIP = split[0];
				int serverPort = Integer.parseInt(split[1]);
				(new CollectoClient(serverIP, serverPort)).start();				
			}
		} else {
			(new CollectoClient()).start();
		}		
	}


	@Override
	//Runnable to start server as thread when testing, otherwise not necessary
	public void run() {
		this.start();
	}
}
