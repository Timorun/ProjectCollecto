package Controller.Server;

import java.io.PrintWriter;
import java.util.Scanner;

import utils.TextIO;

public class CollectoServerTUI {
	
	private PrintWriter console;
	Scanner input = new Scanner(System.in);
	
	public CollectoServerTUI() {
		console = new PrintWriter(System.out, true);
	}
	
	public void showMessage(String message) {
		console.println(message);
	}

	public boolean getBoolean(String string) {
		showMessage(string);
		return TextIO.getlnBoolean();
	}


	public int getInt(String string) {
		showMessage(string);
		return TextIO.getlnInt();
	}

}
