package Controller.AI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import Controller.Client.CollectoClient;
import Model.Board.Board;

public class PointStrategy implements Strategy {
	
	public String getName() {
		return "PointAI";
	}

	
	/**
	 * The deepest out of the 3 strategies (but not the most effective).
	 * Try valid moves on deepcopyboard and find the move that can give you the most points,
	 *  and if same amount of points, the most balls.
	 * 
	 * @return ArrayList<Integer> of best move(s) possible (according to this strategy)
	 */
	public ArrayList<Integer> determineMove(Board board, Collection<Integer> ballscollected) {
		ArrayList<Integer> movelist = new ArrayList<Integer>();
		Board deepcopyboard = board.deepCopy();
		int maxcollected = 0;
		int maxscore = CollectoClient.getPoints(ballscollected);
		ArrayList<Integer> ballsfrommove = new ArrayList<Integer>();
		
		
		if (deepcopyboard.validSingleMoves().size() != 0) {
			ArrayList<Integer> singlemoves = deepcopyboard.validSingleMoves();
			for (Integer move : singlemoves) {
				deepcopyboard.tryMove(move);
				
				// first we collect balls in listofballs then we put them in arraylist ballsfrommove
				//to convert the list of balls collected from HashMapvalues to ArrayList<Integer>,
				// otherwise calling addAll(ballscollected) returns UnsupportedOperationException
				Collection<Integer> listofballs = deepcopyboard.collectBalls();
				ballsfrommove.addAll(listofballs);
				
				int amountcollected = ballsfrommove.size();
				ballsfrommove.addAll(ballscollected);
				int scorepossible = CollectoClient.getPoints(ballsfrommove);
				
				if (scorepossible > maxscore) {
					maxcollected = amountcollected;
					maxscore = scorepossible;
					movelist = new ArrayList<>();
					movelist.add(move);
				} else if (scorepossible == maxscore) {
					if (amountcollected > maxcollected) {						
						maxcollected = amountcollected;
						movelist = new ArrayList<>();
						movelist.add(move);
					}
				}
				deepcopyboard = board.deepCopy();
			}
		
		} else {
			HashMap<Integer, ArrayList<Integer>> doublemoves = board.validDoubleMoves();
			
			for (Integer move1 : doublemoves.keySet()) {
				for (int move2 : doublemoves.get(move1)) {
					deepcopyboard.tryMove(move1, move2);
					
					Collection<Integer> listofballs = deepcopyboard.collectBalls();
					ballsfrommove.addAll(listofballs);
					
					int amountcollected = ballsfrommove.size();
					ballsfrommove.addAll(ballscollected);
					int scorepossible = CollectoClient.getPoints(ballsfrommove);
					
					if (scorepossible > maxscore) {
						maxcollected = amountcollected;
						maxscore = scorepossible;
						movelist = new ArrayList<>();
						movelist.add(move1);
						movelist.add(move2);
					} else if (scorepossible == maxscore) {
						if (amountcollected > maxcollected) {						
							maxcollected = amountcollected;
							movelist = new ArrayList<>();
							movelist.add(move1);
							movelist.add(move2);
						}
					}
					deepcopyboard = board.deepCopy();
				}
			}	
			
		}
		
		return movelist;
	}
	
}
