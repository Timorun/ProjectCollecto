package Controller.AI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import Model.Board.Board;

public class SmartStrategy implements Strategy {
	
	public String getName() {
		return "SmartAI";
	}

	
	/**
	 * Try valid moves on deepcopyboard and find the move that gives you the most balls.
	 * 
	 * @return ArrayList<Integer> of best move(s) possible (according to this strategy)
	 */
	public ArrayList<Integer> determineMove(Board board, Collection<Integer> ballscollected) {
		ArrayList<Integer> movelist = new ArrayList<Integer>();
		Board deepcopyboard = board.deepCopy();
		int maxcollected = 0;
		
		
		if (deepcopyboard.validSingleMoves().size() != 0) {
			ArrayList<Integer> singlemoves = deepcopyboard.validSingleMoves();
			for (Integer move : singlemoves) {
				deepcopyboard.tryMove(move);
				int amountcollected = deepcopyboard.collectBalls().size();
				
				if (amountcollected > maxcollected) {
					maxcollected = amountcollected;
					movelist = new ArrayList<>();
					movelist.add(move);
				}
				deepcopyboard = board.deepCopy();
			}
		
		} else {
			HashMap<Integer, ArrayList<Integer>> doublemoves = board.validDoubleMoves();
			
			for (Integer move1 : doublemoves.keySet()) {
				for (int move2 : doublemoves.get(move1)) {
					deepcopyboard.tryMove(move1, move2);
					int amountcollected = deepcopyboard.collectBalls().size();
					
					if (amountcollected > maxcollected) {
						maxcollected = amountcollected;
						movelist = new ArrayList<>();
						movelist.add(move1);
						movelist.add(move2);
					}
					deepcopyboard = board.deepCopy();
				}
			}	
			
		}
		
		return movelist;
	}
	
	
	
	
	
	
}
