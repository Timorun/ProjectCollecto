package Controller.Exceptions;

public class exitProgram extends Exception{
	private static final long serialVersionUID = 9073041248662660300L;
	
	public exitProgram(String msg) {
		super(msg);
	}
}
