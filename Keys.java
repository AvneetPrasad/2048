package gameLogic;

import java.awt.event.KeyEvent;

/**
 * This class contains the event of when the arrows buttons  being pressed 
 * and update the the whole game
 * 
 * @author Avneet and Kayle
 */

public class Keys {

	public static boolean[] pressed = new boolean[256];
	public static boolean[] prev = new boolean[256];
	/**
         * check for arrow presses then update game accordingly
         */
	public static void update(){
		for(int i = 0; i < 4; i++){
                        //left arrow
			if(i == 0) prev[KeyEvent.VK_LEFT] = pressed[KeyEvent.VK_LEFT];
                        //right arrow
			if(i == 1) prev[KeyEvent.VK_RIGHT] = pressed[KeyEvent.VK_RIGHT];
                        //up arrow
			if(i == 2) prev[KeyEvent.VK_UP] = pressed[KeyEvent.VK_UP];
                        //down arrow
			if(i == 3) prev[KeyEvent.VK_DOWN] = pressed[KeyEvent.VK_DOWN];
		}
	}
	
        /**
         * check for key press
         * @param e 
         */
	public static void keyPressed(KeyEvent e){
		pressed[e.getKeyCode()] = true;
	}
	
        /**
         * check for key release
         * @param e 
         */
	public static void keyReleased(KeyEvent e){
		pressed[e.getKeyCode()] = false;
	}
	
}
