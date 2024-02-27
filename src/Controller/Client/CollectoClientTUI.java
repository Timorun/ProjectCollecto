package Controller.Client;

import java.io.PrintWriter;
import java.util.ArrayList;

import Controller.Exceptions.exitProgram;
import Controller.Exceptions.serverUnavailableException;
import Model.Board.Board;
import utils.TextIO;

public class CollectoClientTUI {
	
	private CollectoClient client;
	
	//set to public in order to test
	public PrintWriter console;
	Board clientboard;
	
	
	public CollectoClientTUI(CollectoClient client) {
		this.client = client;
		console = new PrintWriter(System.out, true);
	}
	
	/**
	 * Starts TUI loop to handle user input.
	 * 
	 * @requires client to be connected to server, if not exit
	 */
	public void start() {
		// Get input from user
		boolean exit = false;
		while (!exit) {
			String input = TextIO.getlnString();
			try {
				handleUserInput(input);
			} catch (exitProgram e) {
				exit = true;
				client.closeConnection();
			} catch (serverUnavailableException e) {
				console.println("Server unavailable");
				exit = true;
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Prints string to console.
	 */
	public void showMessage(String string) {
		console.println(string);
	}
	
	/**
	 * Print commands depending on if client is Human
	 * or Computer(instance of CollectoComputerClient).
	 */
	public void printMenu() {
		String commands = "\nCommands: \n";
		if (!(this.client instanceof CollectoComputerClient)) {
			commands += "Move x ............... do single move 'x'\n"
					+ "Move x and y ......... do double move x then y\n"
					+ "Hint ................. get move hints for current board\n";
		}
		commands += "Login username ....... login with username \n"
				+ "List ................. requests list of clients\n"
				+ "Queue ................ queues you up for a game\n"
				+ "Help ................. print this menu again\n"
				+ "Exit ................. disconnect and exit\n"
				+ "If you desire to quit the game prematurely, simply terminate console\n"
				+ "Please note commmands are not case sensitive!\n";
		console.println(commands);
	}

	public boolean getBoolean(String string) {
		showMessage(string);
		return TextIO.getlnBoolean();
	}

	public String getString(String question) {
		showMessage(question);
		return TextIO.getlnString();
	}

	public int getInt(String string) {
		showMessage(string);
		return TextIO.getlnInt();
	}
	
	public void setupBoard(ArrayList<Integer> serverfields) {
		this.clientboard = new Board(serverfields);
	}
	
	public void displayBoard() {
		console.println("New board state:\n" + this.clientboard);
	}
	
	/**
	 * Called in start loop to handleUserInput in TUI.
	 * Acts accordingly to input.
	 */
	public void handleUserInput(String input) throws exitProgram, serverUnavailableException {
		// implement switch
		String command = input.toUpperCase();
		String[] cmdsplit = command.split(" ");
		
		
		switch (cmdsplit[0]) {
			case "HINT":
				if (clientboard.validSingleMoves().size() != 0) {
					console.println("You should make a singlemove with one of these values:\n" 
							+ clientboard.validSingleMoves().toString());
				} else if (clientboard.validDoubleMoves().size() != 0) {
					console.println("You should make a doublemove with"
						+ " one of these move combinations:\n" 
						+ clientboard.validDoubleMoves().toString()
						+ "\n Right above you see a hashmap with first moves as keys, and" 
						+ "possible second move(s) as values for each key");
				}
				break;
				
			case "LOGIN":
				if (cmdsplit.length == 2) {
					client.username = cmdsplit[1];
					showMessage("Username set to: " + this.client.username);
					client.sendMessage("LOGIN~" + cmdsplit[1]);
				} else {
					console.println("Format not correct, try 'help' command");
				}
				break;
				
			case "QUEUE":
				client.sendMessage("QUEUE");
				console.println("Now trying to queue up");
				break;
				
			case "LIST":
				client.sendMessage("LIST");
				break;
				
			case "EXIT":
				this.showMessage("Disconnecting from server");
				client.closeConnection();
				break;
				
			case "HELP":
				this.printMenu();
				break;
				
			case "MOVE":
				int move1 = 28;
				int move2 = 28;
				String movecmd = "MOVE~";
				if (cmdsplit.length > 4 || cmdsplit.length == 3) {
					console.println("Wrong format, try 'help'command");
					break;
				}
				
				//singlemove
				if (cmdsplit.length == 2) {
					try {
						move1 = Integer.parseInt(cmdsplit[1]);
					} catch (NumberFormatException e) {
						console.println("Please input a move int, try 'help' command");
						return;
					}
					if (move1 < 0 || move1 > 27) {
						console.println("Move not between 0 and 27");
						return;
					}
					if (!clientboard.validSingleMoves().contains(move1)) {
						console.println("Invalid move");
						return;
					} else {
						movecmd += move1;						
					}
					
				
					
				//doublemove
				} else if (cmdsplit.length == 4) {
					if (!cmdsplit[2].equals("AND")) {
						console.println("Please use correct format, try 'help' command");
						return;
					}
					try {
						move1 = Integer.parseInt(cmdsplit[1]);
						move2 = Integer.parseInt(cmdsplit[3]);
					} catch (NumberFormatException e) {
						console.println("Please input a second move int, try 'help' command");
						return;
					}
					if (move1 < 0 || move1 > 27 || move2 < 0 || move2 > 27) {
						console.println("Move(s) not between 0 and 27");
						return;
					}
					if (clientboard.validDoubleMoves().containsKey(move1)) {
						if (clientboard.validDoubleMoves().get(move1).contains(move2)) {
							//this means doublemove by user is valid
							movecmd += move1 + "~" + move2;	
						}
					} else {
						console.println("Invalid move");
						return;
					}
				}
				client.sendMessage(movecmd);
				break;
				
				
			default: console.println("Command not recogized, try 'help' command"); 
		}
	}
}
