package Model.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Board {
	public static final int DIM = 7;

	private int[][] fields = new int[DIM][DIM];
	private int[][] fieldscopy = new int[DIM][DIM];
	

	private static final String DELIM = "  ";
	private static final String LINE = "+--+--+--+--+--+--+---+";
	
	/**
	 * Constructor creating random valid Board.
	 */
	public Board() {
		this.resetBoard();
	}
	
	/**
	 * Create Board with known fields for clientboard.
	 * 
	 * @requires List of integer to be of size 
	 * @param List of integers, fields
	 */
	public Board(ArrayList<Integer> serverfields) {
		int index = 0;
		for (int row = 0; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				this.fields[row][col] = serverfields.get(index);
				index++;
			}
		}
	}
	

	
	/**
	 * Checks if field is empty.
	 * 
	 * @requires row and col to be between 0 and 6
	 * @param row
	 * @param col
	 * @return true if field==0 else false
	 */
	public boolean isEmptyField(int row, int col) {
		return this.getField(row, col) == 0;
	}

	
	/**
	 * 
	 * @requires row and col to be between 0 and 6
	 * @param row
	 * @param col
	 * @return int representing color of that field
	 */
	public int getField(int row, int col) {
		return this.fields[row][col];
	}
	
	
	/**
	 * @requires Int N to be between 0 and 27
	 * @ensures Shifts row right/left, or column down/up
	 * 
	 * @param N
	 * @return
	 */
	private synchronized void singleMove(int N) {		
		//push row N to left
		if (N >= 0 && N <= 6) {
			int row = N;
			for (int col = 0; col < DIM - 1; col++) {
				if (fields[row][col] == 0) {
					int y;
					for (y = col + 1; y < DIM - 1 && fields[row][y] == 0; y++) {
					}
					fields[row][col] = fields[row][y];
					fields[row][y] = 0;
				}
			}
		}
		
		//push row (N-7) to right
		if (N >= 7  && N <= 13) {
			int row = N - 7;
			for (int col = DIM - 1; col >= 1; col--) {
				if (fields[row][col] == 0) {
					int y;
					for (y = col - 1; y > 0 && fields[row][y] == 0; y--) {
					}
					fields[row][col] = fields[row][y];
					fields[row][y] = 0;
				}
			}
		}
		
		
		//push column (N-14) upwards
		if (N >= 14 && N <= 20) {
			int col = N - 14;
			for (int row = 0; row < DIM - 1; row++) {
				if (fields[row][col] == 0) {
					int x;
					for (x = row + 1; x < DIM - 1 && fields[x][col] == 0; x++) {
					}
					fields[row][col] = fields[x][col];
					fields[x][col] = 0;
				}
			}
		}
		
		//push column (N-21) downwards
		if (N >= 21 && N <= 27) {
			int col = N - 21;
			for (int row = DIM - 1; row >= 1; row--) {
				if (fields[row][col] == 0) {
					int x;
					for (x = row - 1; x > 0 && fields[x][col] == 0; x--) {
					}
					fields[row][col] = fields[x][col];
					fields[x][col] = 0;
				}
			}
		}
	}

	
	/**
	 * Makes single move only if the move is valid.
	 *  
	 * @param a
	 * @return true if move is valid, else false
	 */
	public synchronized boolean tryMove(int n) {
		arrayCopy(fields, fieldscopy);
		this.singleMove(n);
		if (this.boardHasAdjacent()) {
			return true;
		} else {
			arrayCopy(fieldscopy, fields);
			return false;
		}
	}
	
	
	/**
	 * Makes double move only if the doublemove is valid.
	 * @requires No valid single moves
	 * 
	 * @param a
	 * @param b
	 * @return true if doublemove made, else false
	 */
	public synchronized boolean tryMove(int a, int b) {
		if (this.validSingleMoves().size() != 0) {
			return false;
		}
		arrayCopy(fields, fieldscopy);
		
		this.singleMove(a);
		this.singleMove(b);
		
		if (this.boardHasAdjacent()) {
			return true;
		} else {
			this.fields = fieldscopy.clone();
			return false;
		}
	}	
	
	
	/**
	 * Method to find all possible singlemoves, stored in ArrayList of type integer.
	 * With key as first move and int array of second moves as value.
	 * @requires No singlemoves possible otherwise 0 doublemoves allowed
	 * 
	 * @return all possible doubleMoves
	 */
	public ArrayList<Integer> validSingleMoves() {
		ArrayList<Integer> singlemoves = new ArrayList<Integer>();
		arrayCopy(fields, fieldscopy);
		
		for (int n = 0; n <= 27; n++) {
			this.singleMove(n);
			if (this.boardHasAdjacent()) {
				singlemoves.add(n);
			}
			arrayCopy(fieldscopy, fields);	
		}
		return singlemoves;
	}
	
	/**
	 * Method to find all possible doublemoves, stored in HashMap.
	 * With key as first move and ArrayList of Integers of second moves as value.
	 * @requires No singlemoves possible otherwise 0 doublemoves allowed
	 * 
	 * @ensures key always has at least 1 value. Keys and int(s) in value always between 0 and 27.
	 * 
	 * @return all possible doubleMoves with current board.
	 */
	public HashMap<Integer, ArrayList<Integer>> validDoubleMoves() {
		HashMap<Integer, ArrayList<Integer>> doublemoves 
			= new HashMap<Integer, ArrayList<Integer>>();
		
		if (this.validSingleMoves().size() == 0) {
			for (int a = 0; a <= 27; a++) {
				for (int b = 0; b <= 27; b++) {
					arrayCopy(fields, fieldscopy);
					this.singleMove(a);
					this.singleMove(b);
					if (this.boardHasAdjacent()) {			
						if (!doublemoves.containsKey(a)) {
							doublemoves.put(a, new ArrayList<Integer>());
						}
						doublemoves.get(a).add(b);					
					}
					arrayCopy(fieldscopy, fields);
					
				}
			}
		}
		return doublemoves;
	}
	
	
	/**
	 * Collect all coordinates of balls that are adjacent to another ball of same colour.
	 * Coords stored in HashMap in form of, row:col as key to avoid duplicates,
	 *  and with the color of each ball (here coords) as value.
	 * 
	 * 
	 * @return Collection of integers, representing balls collected
	 */
	public Collection<Integer> collectBalls() {		
		HashMap<String, Integer> balls = new HashMap<String, Integer>();
		
		
		// Check if there is 2 same color horizontally
		for (int row = 0; row < DIM; row++) {
			for (int col = 0; col < DIM - 1; col++) {
				if (fields[row][col] != 0 && fields[row][col] == fields[row][col + 1]) {
					
					
					int col2 = col + 1;
					balls.put(row + ":" + col, fields[row][col]);
					balls.put(row + ":" + col2, fields[row][col2]);
				}
			}
		}
		
		
		// Check if there is 2 same color vertically
		for (int col = 0; col < DIM; col++) {
			for (int row = 0; row < DIM - 1; row++) {
				if (fields[row][col] != 0 && fields[row][col] == fields[row + 1][col]) {

					int row2 = row + 1;
					balls.put(row + ":" + col, fields[row][col]);
					balls.put(row2 + ":" + col, fields[row2][col]);
				}
			}
		}
		
		for (String ballcoords : balls.keySet()) {
			String[] splitcoords = ballcoords.split(":");
			int row = Integer.parseInt(splitcoords[0]);
			int col = Integer.parseInt(splitcoords[1]);
			this.fields[row][col] = 0;
		}
		
		
		return balls.values();
	}
	
	
	
	/**
	 * Sets up board.
	 * 
	 * @ensures this.board is Valid board
	 */
	private void resetBoard() {
		// make list of balls that should be in the board
		ArrayList<Integer> allballs = new ArrayList<Integer>();
		for (int i = 0; i < 8; i++) {
			allballs.add(1);
			allballs.add(2);
			allballs.add(3);
			allballs.add(4);
			allballs.add(5);
			allballs.add(6);
		}
		
		do {
			Collections.shuffle(allballs);
			int index = 0;
			for (int row = 0; row < 7; row++) {
				for (int col = 0; col < 7; col++) {
					if (row == 3 && col == 3) {
						this.fields[row][col] = 0;
					} else {
						this.fields[row][col] = allballs.get(index);
						index++;
					}
				}
			}
		} while (this.boardHasAdjacent());
	}

	
	
	private boolean boardHasAdjacent() {
		// Check if there is 2 same color horizontally
		for (int row = 0; row < DIM; row++) {
			for (int col = 0; col < DIM - 1; col++) {
				if (fields[row][col] != 0 && fields[row][col] == fields[row][col + 1]) {
					return true;
				}
			}
		}
		
		// Check if there is 2 same color vertically
		for (int col = 0; col < DIM; col++) {
			for (int row = 0; row < DIM - 1; row++) {
				if (fields[row][col] != 0 && fields[row][col] == fields[row + 1][col]) {
					return true;
				}
			}
		}
		return false;		
	}
	
	
	/**
	 * Checks if game is over thanks to validSingleMoves and validDoubleMoves.
	 * 
	 * @requires game to be valid at the start
	 */
	public boolean gameOver() {
		if (this.validSingleMoves().size() == 0 && this.validDoubleMoves().size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	
	
	/**
	 * Override of default to string that presents the board
	 * with around it the indices of all moves.
	 */
	public String toString() {
		String result = "";
		result += "    " + 21;
		for (int i = 22; i <= 27; i++) {
			result += " " + i;
		}
		result += "\n   " + LINE + "\n";
		int rightmove = 7;
		int leftmove = 0;
		for (int row = 0; row < DIM; row++) {
			for (int col = 0; col < DIM; col++) {
				if (col == 0) {
					result += String.format("%2d |", rightmove) + " " + fields[row][col];
					rightmove++;
					
				} else {
					result += DELIM + fields[row][col];
				}
			}
			result += " " + String.format("|%2d", leftmove) + "\n";
			leftmove++;
		}
		result += "   " + LINE + "\n";
		result += "    " + 14;
		for (int i = 15; i <= 20; i++) {
			result += " " + i;
		}
		return result;
	}

	//
	private static void arrayCopy(int[][] source, int[][] destination) {
	    for (int i = 0; i < source.length; i++) {
	        System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
	    }
	}
	
	public Board deepCopy() {
		Board board = new Board();
		for (int row = 0; row < DIM; row++) {
			for (int col = 0; col < DIM; col++) {
				board.fields[row][col] = fields[row][col];
			}
		}
		return board;
	}
}
