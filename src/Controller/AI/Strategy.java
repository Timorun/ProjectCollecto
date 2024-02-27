package Controller.AI;

import java.util.ArrayList;
import java.util.Collection;

import Model.Board.Board;

/**
 * Strategy Interface to be implemented by the 3 AI strategies.
 */
public interface Strategy {
	
	public String getName();
	public ArrayList<Integer> determineMove(Board board, Collection<Integer> balls);
	
}
