package gameLogic;

import java.io.IOException;
import java.sql.SQLException;
import javax.swing.JFrame;

/**
 * This class contains the main method which is used to start the game
 * @author Avneet and Kayle
 */
public class Start {

	public static void main(String[] args) throws SQLException, IOException{
		Game game = new Game();
                
		
		JFrame window = new JFrame("2048: Rise of Darkness");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(true);
		window.add(game);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		game.start();
	}
}