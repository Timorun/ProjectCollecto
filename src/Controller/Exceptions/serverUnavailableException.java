package Controller.Exceptions;

public class serverUnavailableException extends Exception {

	private static final long serialVersionUID = 3194494346431589825L;

	public serverUnavailableException(String msg) {
		super(msg);
	}

}
