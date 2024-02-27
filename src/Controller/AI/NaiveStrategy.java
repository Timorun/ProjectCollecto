package Controller.AI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import Model.Board.Board;

public class NaiveStrategy implements Strategy {
	
	
	public String getName() {
		return "NaiveAI";
	}

	/** 
	 * determine move with Naive Strategy, does a random move out of possible moves.
	 * does not take in mind balls already collected
	 * 
	 * @requires Move to be possible
	 */
	public ArrayList<Integer> determineMove(Board board, Collection<Integer> balls) {
		ArrayList<Integer> movelist = new ArrayList<Integer>();
		
		if (board.validSingleMoves().size() != 0) {
			ArrayList<Integer> singlemoves = board.validSingleMoves();
		    int randmove = singlemoves.get(new Random().nextInt(singlemoves.size()));
			movelist.add(randmove);
		
		} else {
			HashMap<Integer, ArrayList<Integer>> doublemoves = board.validDoubleMoves();
			
			// get random key(so first move) of doublemoves possible
			Object[] keys = doublemoves.keySet().toArray();
			int move1 = (int) keys[new Random().nextInt(keys.length)];
			
			// get random value of possible second moves after move1
			ArrayList<Integer> secondmoves = doublemoves.get(move1);
		    int move2 = secondmoves.get(new Random().nextInt(secondmoves.size()));
			
		    movelist.add(move1);
		    movelist.add(move2);        
		}
		
		return movelist;
	}

}
