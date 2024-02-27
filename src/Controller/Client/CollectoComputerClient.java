package Controller.Client;

import java.util.ArrayList;

import Controller.AI.NaiveStrategy;
import Controller.AI.PointStrategy;
import Controller.AI.SmartStrategy;
import Controller.AI.Strategy;
import Controller.Exceptions.serverUnavailableException;
import utils.TextIO;

public class CollectoComputerClient extends CollectoClient {
	private Strategy strategy;
	
	// Normal constructors called when running class
	public CollectoComputerClient() {
		super();
	}
	public CollectoComputerClient(String serverIP, int serverPort) {
		super(serverIP, serverPort);
	}
	
	//Constructor called in testing
	public CollectoComputerClient(String serverIP, int serverPort, Strategy strategy) {
		super(serverIP, serverPort);
		this.strategy = strategy;
	}
	
	/**
	 * Called in main before starting client, in order to set a Strategy.
	 * @ensures determineMove works, and does move according to strategy chosen
	 */
	public void setStrategy() {
		int strat = 0;
		while (strat != 1 && strat != 2 && strat != 3) {
			strat = this.view.getInt("Which strategy do you want to use ?\n"
					+ "Press 1 for Naive, 2 for Smart and 3 for Point");
			if (strat == 1) {
				this.strategy = new NaiveStrategy();
			} else if (strat == 2) {
				this.strategy = new SmartStrategy();
			} else if (strat == 3) {
				this.strategy = new PointStrategy();
			}
		}
	}
	
	
	@Override
	/**
	 * DetermineMove called in Client when its our turn to make move
	 * Uses set strategy and current clientboard to determine strategy
	 * Finds best move according to startegy and sends it to server
	 * 
	 * @requires move to be possible, otherwise no move is made
	 */
	public void determineMove() {
		view.showMessage("Your turn, make a move " + username);
		ArrayList<Integer> movelist = this.strategy.determineMove(view.clientboard, this.myballs);
		try {
			if (movelist.size() == 2) {
				int move1 = movelist.get(0);
				int move2 = movelist.get(1);
				this.sendMessage("MOVE~" + move1 + "~" + move2);
			} else if (movelist.size() == 1) {
				int move1 = movelist.get(0);
				this.sendMessage("MOVE~" + move1);
			} else {
				this.view.showMessage("No move found");
			}
		} catch (serverUnavailableException e) {
			this.view.showMessage("Could not send message to server, server unavailable");
		}
	}
	
	/**
	 * Main asks for ip:port if user does not want to play on UT server.
	 * Creates CollectoComputerClient
	 * Then calls setStrategy to set which AI strategy user wants to use.
	 * Launches CollectoComputerClient
     */
	public static void main(String[] args) {
		CollectoComputerClient computerClient;
		System.out.println("Do you want to play on UT reference server ?");
		if (!TextIO.getBoolean()) {
			System.out.println("Please type in ipadress:port");
			String adress = "";
			while (adress.equals("")) {				
				adress = TextIO.getlnString();
			}
			String[] split = adress.split(":");
			String serverIP = split[0];
			int serverPort = Integer.parseInt(split[1]);
			computerClient = new CollectoComputerClient(serverIP, serverPort);
		} else {
			computerClient = new CollectoComputerClient();
		}
		computerClient.setStrategy();
		computerClient.start();
	}
	
	
}
