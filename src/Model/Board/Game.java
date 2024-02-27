package Model.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import Controller.Server.CollectoClientHandler;

public class Game implements Runnable {

	public static final int NUMBER_PLAYERS = 2;
	
    /**
     * The board.
     * @invariant board is never null
     */
    private Board board;
    
    // variable initialized as null
    // when client shuts down it will automatically send disconnect(this)
    // and assign that client to this variable.
    private CollectoClientHandler disconnected = null;
    
    /**
     * Index of the current player.
     * @invariant the index is always between 0 and NUMBER_PLAYERS
     */
    private int current;
    	
    /**
     * The 2 players of the game.
     * @invariant the length of the array equals NUMBER_PLAYERS
     * @invariant all array items are never null
     */
    private CollectoClientHandler[] clients;
    
    private boolean run;
    
    private ArrayList<Integer> player1balls = new ArrayList<Integer>();
    private ArrayList<Integer> player2balls = new ArrayList<Integer>();
    
    
    
    /**
     * Creates a new Game object.
     * @requires s0 and s1 to be non-null
     * @param s0 the first player
     * @param s1 the second player
     */
    public Game(CollectoClientHandler c0, CollectoClientHandler c1) {
        clients = new CollectoClientHandler[2];
        clients[0] = c0;
        clients[1] = c1;
    }    

    /**
     * Called when Game thread starts.
     * Creates board and sets clienthandlers' game to this
     * Runs until player disconnects, or run set to false(possible with method checkIfOver)
     */
	@Override
	public void run() {
		this.board = new Board();
		this.reset();
		clients[0].setGame(this);
		clients[1].setGame(this);
		this.sendNewGame();
		run = true;
		
		while (run) {
			if (disconnected != null) {
				if (clients[0] == disconnected) {
					clients[1].sendMessage("GAMEOVER~DISCONNECT~" + clients[1].getUsername());
				} else {
					clients[0].sendMessage("GAMEOVER~DISCONNECT~" + clients[0].getUsername());
				}
				clients[0].setGame(null);
				clients[1].setGame(null);
				run = false;
			}
		}
	}
	
	/**
	 * Method to get result string
	 * Called by checkifOver() when game has no more moves possible.
	 * if 1 player wins then methods returns "VICTORY~winner's username"
	 * if draw then return "DRAW"
	 * 
	 * @requires player1balls and player2balls to no be null
	 * @return string result
	 * @ensures Player with most points is declared winner, If same amount of points then DRAW
	 */
	private String getResult() {
		int player1score = getPoints(player1balls);
		int player2score = getPoints(player2balls);
		
		if (player1score > player2score) {
			return "VICTORY~" + clients[0].getUsername();
		} else if (player2score > player1score) {
			return "VICTORY~" + clients[1].getUsername();
		} else if (player1balls.size() > player2balls.size()) {
			return "VICTORY~" + clients[0].getUsername();
		} else if (player2balls.size() > player2balls.size()) {
			return "VICTORY~" + clients[1].getUsername();
		} else {
			return "DRAW";			
		}
	}
	
    /**
     * Called by clientHandler to disconnect that client from this Game
     * Sets disconnected variable to client from param.
     * Subsquently ending the game
     * 
     * @ensures Game is ended when a client disconnects
     */
    public void disconnect(CollectoClientHandler client) {
    	this.disconnected = client;
    }

	/**
     * Resets the game.
     * The board is emptied and client[0] becomes the current player.
     */
    private void reset() {
    	player1balls = new ArrayList<Integer>();
    	player2balls = new ArrayList<Integer>();
        current = 0;
    }
    
    
    /**
     * Method to send msg to both clients in this game.
     * @param msg
     * @requires msg to be not null
     */
    public void sendtoBoth(String msg) {
    	clients[0].sendMessage(msg);
		clients[1].sendMessage(msg);
    }
    
    
    /**
     * Sends newgame message to clients connected
     * With all fields and the name of each client.
     * @return
     */
    private void sendNewGame() {
    	String result = "NEWGAME";
    	for (int row = 0; row < 7; row++) {
    		for (int col = 0; col < 7; col++) {
    			result += "~" + board.getField(row, col);
    		}
    	}
    	result += "~" + clients[0].getUsername() + "~" + clients[1].getUsername();
    	System.out.println("Starting board" + board.validSingleMoves() + "\n" + board);
    	this.sendtoBoth(result);
    }
    
    
    
    /**
     * Checks if its client's turn and then checks if move is valid.
     * If not his turn error not turn sent
     * If not valid error not valid sent
     * if valid move(s) move sent to both clients in the game 
     * and collectedballs added to that player
     * 
     * @param client
     * @param move
     */
    public synchronized void getMove(CollectoClientHandler client, ArrayList<Integer> movelist) {
    	boolean movemade = false;
    	if (clients[current] != client) {
    		client.sendError("Not your turn");
    	} else {
    		if (movelist.size() == 2) {
    			int move1 = movelist.get(0);
    			int move2 = movelist.get(1);
    			movemade = board.tryMove(move1, move2);
    			if (movemade) {
    				this.sendtoBoth("MOVE~" + move1 + "~" + move2);
    			} else {
    				client.sendError("Move invalid");
    			}
    		} else if (movelist.size() == 1) {
    			int move = movelist.get(0);
    			movemade = board.tryMove(move);
    			if (movemade) {
    				this.sendtoBoth("MOVE~" + move);
    			} else {
    				client.sendError("Move invalid");
    			}
    		}
    		if (movemade) {
    			Collection<Integer> newballs = board.collectBalls();
    			if (current == 0) {
    				player1balls.addAll(newballs);
    			} else {
    				player2balls.addAll(newballs);
    			}
    			current = 1 - current;
    			this.checkIfOver();
    		}
    	}
    }
    
    /**
     * Called after each move to check if game is over.
     */
    public void checkIfOver() {
    	if (this.board.gameOver()) {
    		sendtoBoth("GAMEOVER~" + getResult());
    		clients[0].setGame(null);
    		clients[1].setGame(null);
    		this.run = false;
    	}  	
    }
    
    
    /**
     * Simple static method to getscore out of balls list.
     * @param collected
     * @return score
     */
    private static int getPoints(ArrayList<Integer> collected) {
    	int score = 0;
    	for (int i = 1; i <= 6; i++) {
    		int occurrences = Collections.frequency(collected, i);
    		if (occurrences == 6) {
    			score += 2;
    		} else if (occurrences >= 3) {
    			score += 1;
    		}
    		
    	}
    	return score;
    }


}
