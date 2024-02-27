package Controller.Server;

import Model.Board.Game;

/**
 * continously checks if queue is bigger than 1 if so start game if queue has 
 * 2 clienthandlers then use start game in server
 *  with 2 clienthandlers as parameters remove these 2 from queue.
 */
public class QueueChecker implements Runnable {
	CollectoServer srv;
	
	/**
	 * Constructor of queue checker taking server as parameter
	 * in order to handle that server's queue.
	 * @param server
	 */
	public QueueChecker(CollectoServer server) {
		this.srv = server;
	}
	
	@Override
	/**
	 * Thread loop that checks queue every 2 seconds
	 * if queue has 2 clients(clienthandlers) then start a Game thread
	 * with the 2 first clients as parameters.
	 * 
	 * @requires Server to handle queue message properly, or Game never started
	 */
	public void run() {
		while (true) {
			if (srv.queue.size() >= 2) {
				new Thread(new Game(srv.queue.get(0), srv.queue.get(1))).start();
				srv.queue.remove(0);
				srv.queue.remove(0);
			}
			// queue does not have to run as fast as possible, so in order to make it
			// less heavy on the system we make it check every 2 seconds
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted");
			}
		}
		
	}

}
