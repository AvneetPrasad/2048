package gameLogic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.sound.sampled.Clip;
/**
 * This class contains the main body of the game while enables the audio files,
 * creating the table, enabling the tiles, checking if the tiles is possible to combine 
 * check if the player lost or win, aquire the score during the game.
 * 
 * @author Avneet and Kayle
 */

/**
 * manupulating the class moving etc
 * 
 */
public class GameBoard { 

	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int UP = 2;
	public static final int DOWN = 3;

	public static final int ROWS = 4;
	public static final int COLS = 4;

	private final int startingTiles = 2;
	private Tile[][] board;
	private boolean dead;
	private boolean won;
	private BufferedImage gameBoard; 
        // background if the title not there it would be at the back
	private int x;
	private int y;

	private static int SPACING = 10; // pix
	public static int BOARD_WIDTH = (COLS + 1) * SPACING + COLS * Tile.WIDTH;
	public static int BOARD_HEIGHT = (ROWS + 1) * SPACING + ROWS * Tile.HEIGHT;

	private long elapsedMS;
	private long startTime;
	private boolean hasStarted; // for the timer 

	private ScoreManager scores;
	private Leaderboards lBoard;
	private AudioHandler audio;
	private int saveCount = 0;
        
        /**
         * apply the score in the leaderboard and check if game has won or lose
         * @param x
         * @param y 
         */
	public GameBoard(int x, int y) {
		this.x = x;
		this.y = y;
		board = new Tile[ROWS][COLS];
		gameBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
		createBoardImage();

                //-------------audio code ------------//
		audio = AudioHandler.getInstance();
		audio.load("click.wav", "click");
		audio.load("MainSong.mp3", "BG");
		audio.adjustVolume("BG", -10);
		audio.play("BG", Clip.LOOP_CONTINUOUSLY);

		lBoard = Leaderboards.getInstance();
		lBoard.loadScores();
		scores = new ScoreManager(this);
		scores.loadGame();
		scores.setBestTime(lBoard.getFastestTime());
		scores.setCurrentTopScore(lBoard.getHighScore());
		if(scores.newGame()){
			start();
			scores.saveGame();
		}
		else{
			for(int i = 0; i < scores.getBoard().length; i++){
				if(scores.getBoard()[i] == 0) continue;
				spawn(i / ROWS, i % COLS, scores.getBoard()[i]);
			}
			// not calling setDead because we don't want to save anything
			dead = checkDead();
			// not coalling setWon because we don't want to save the time
			won = checkWon();
		}
	}
        /**
         * reset the game
         */
	public void reset(){
		board = new Tile[ROWS][COLS];
		start();
		scores.saveGame();
		dead = false;
		won = false;
		hasStarted = false;
		startTime = System.nanoTime();
		elapsedMS = 0;
		saveCount = 0;
	}
        /**
         * start the game
         */
	private void start() {
		for (int i = 0; i < startingTiles; i++) {
			spawnRandom();
		}
	}

	/** 
         * -----------Debug method-----
         * @param row
         * @param col
         * @param value 
         */
	private void spawn(int row, int col, int value) {
		board[row][col] = new Tile(value, getTileX(col), getTileY(row));
	}
        
        /**
         * create the board game
         */       
	private void createBoardImage() {
            // this 9 lines of code creates the background of the gameboard
            // the 16 titles and the background
		Graphics2D g = (Graphics2D) gameBoard.getGraphics();
		g.setColor(Color.darkGray); // background color
		g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
		g.setColor(Color.gray);// titles color

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int x = SPACING + SPACING * col + Tile.WIDTH * col; // spacing = 1 then x position will be 100
				int y = SPACING + SPACING * row + Tile.HEIGHT * row;
				g.fillRoundRect(x, y, Tile.WIDTH, Tile.HEIGHT, Tile.ARC_WIDTH, Tile.ARC_HEIGHT);
			}
		}
	}
       /**
        * update the whole game 
        */
	public void update() {
		saveCount++;
		if (saveCount >= 120) {
			saveCount = 0;
			scores.saveGame();
		}		
		if (!won && !dead) {
			if (hasStarted) {
				elapsedMS = (System.nanoTime() - startTime) / 1000000;
				scores.setTime(elapsedMS);
			}
			else {
				startTime = System.nanoTime();
			}
		}
		checkKeys();// check if any key has been pressed

		if (scores.getCurrentScore() > scores.getCurrentTopScore()) {
			scores.setCurrentTopScore(scores.getCurrentScore());
		}                                
                // now check if the game has been won
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				Tile current = board[row][col];
				if (current == null) continue;
				current.update();
                                
                                //reset position                                
				resetPosition(current, row, col);
				if (current.getValue() == 2048) {
					setWon(true);
				}
			}
		}
	}
        /**
         * render the game board 
         * @param g 
         */
	public void render(Graphics2D g) {
		BufferedImage finalBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) finalBoard.getGraphics();
		g2d.setColor(new Color(0, 0, 0, 0));
		g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
		g2d.drawImage(gameBoard, 0, 0, null);

                // place to draw tiles
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				Tile current = board[row][col];
				if (current == null) continue;
				current.render(g2d);
			}
		}

		g.drawImage(finalBoard, x, y, null);
		g2d.dispose();

		
	}
        /**
         * once the game is done back to 0 tiles 
         * @param tile
         * @param row
         * @param col 
         */
	private void resetPosition(Tile tile, int row, int col) {
		if (tile == null) return;

		int x = getTileX(col);
		int y = getTileY(row);

		int distX = tile.getX() - x;
		int distY = tile.getY() - y;

		if (Math.abs(distX) < Tile.SLIDE_SPEED) {
			tile.setX(tile.getX() - distX); // only move 10 pix 
		}

		if (Math.abs(distY) < Tile.SLIDE_SPEED) {
			tile.setY(tile.getY() - distY); // only move 10 pix 
		}

		if (distX < 0) { // move to left
			tile.setX(tile.getX() + Tile.SLIDE_SPEED);
		}
		if (distY < 0) { // move to right
			tile.setY(tile.getY() + Tile.SLIDE_SPEED);
		}
		if (distX > 0) { // move to up
			tile.setX(tile.getX() - Tile.SLIDE_SPEED);
		}
		if (distY > 0) { // move to down
			tile.setY(tile.getY() - Tile.SLIDE_SPEED);
		}
	}
        /**
         * get the tiles on specific x axis
         * @param col
         * @return 
         */
	public int getTileX(int col) {
		return SPACING + col * Tile.WIDTH + col * SPACING;
	}
        /**
         * get the tiles on specific y axis
         * @param col
         * @return 
         */
	public int getTileY(int row) {
		return SPACING + row * Tile.HEIGHT + row * SPACING;
	}
        /**
         * check if the tiles moving is in the specific bounds
         * @param direction
         * @param row
         * @param col
         * @return 
         */
	private boolean checkOutOfBounds(int direction, int row, int col) {
		if (direction == LEFT) {
			return col < 0;
		}
		else if (direction == RIGHT) {
			return col > COLS - 1;
		}
		else if (direction == UP) {
			return row < 0;
		}
		else if (direction == DOWN) {
			return row > ROWS - 1;
		}
		return false;
	}
        /**
         * is it possible to move the tile
         * @param row
         * @param col
         * @param horizontalDirection
         * @param verticalDirection
         * @param direction
         * @return 
         */
	private boolean move(int row, int col, int horizontalDirection, int verticalDirection, int direction) {
		boolean canMove = false;
		Tile current = board[row][col];
		if (current == null) return false;
		boolean move = true;
		int newCol = col;
		int newRow = row;
		while (move) {
			newCol += horizontalDirection;
			newRow += verticalDirection;
			if (checkOutOfBounds(direction, newRow, newCol)) break;
			if (board[newRow][newCol] == null) {
				board[newRow][newCol] = current;
				canMove = true;
				board[newRow - verticalDirection][newCol - horizontalDirection] = null;
				board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
			}
			else if (board[newRow][newCol].getValue() == current.getValue() && board[newRow][newCol].canCombine()) {
				board[newRow][newCol].setCanCombine(false);
				board[newRow][newCol].setValue(board[newRow][newCol].getValue() * 2);
				canMove = true;
				board[newRow - verticalDirection][newCol - horizontalDirection] = null;
				board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
				board[newRow][newCol].setCombineAnimation(true);
				scores.setCurrentScore(scores.getCurrentScore() + board[newRow][newCol].getValue());
			}
			else {
				move = false;
			}
		}
		return canMove;
	}
        /**
         *  move tiles 
         * @param direction 
         */
	public void moveTiles(int direction) {
		boolean canMove = false;
		int horizontalDirection = 0;
		int verticalDirection = 0;

                //---------------this is only if the direction on the left can move to or not ------------------//
		if (direction == LEFT) {
			horizontalDirection = -1;
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					if (!canMove)
						canMove = move(row, col, horizontalDirection, verticalDirection, direction);
					else move(row, col, horizontalDirection, verticalDirection, direction);
				}
			}
		}
                //---------------this is only if the direction on the right can move to or not ------------------//
		else if (direction == RIGHT) {
			horizontalDirection = 1;
			for (int row = 0; row < ROWS; row++) {
				for (int col = COLS - 1; col >= 0; col--) {
					if (!canMove)
						canMove = move(row, col, horizontalDirection, verticalDirection, direction);
					else move(row, col, horizontalDirection, verticalDirection, direction);
				}
			}
		}
                //---------------this is only if the direction on the up can move to or not ------------------//
		else if (direction == UP) {
			verticalDirection = -1;
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					if (!canMove)
						canMove = move(row, col, horizontalDirection, verticalDirection, direction);
					else move(row, col, horizontalDirection, verticalDirection, direction);
				}
			}
		}
                //---------------this is only if the direction on the down can move to or not ------------------//
		else if (direction == DOWN) {
			verticalDirection = 1;
			for (int row = ROWS - 1; row >= 0; row--) {
				for (int col = 0; col < COLS; col++) {
					if (!canMove){
						canMove = move(row, col, horizontalDirection, verticalDirection, direction);
                                        // if the space is available to move to it will become that position 
                                        }
                                        // else it wont move at all
                                        else move(row, col, horizontalDirection, verticalDirection, direction);
				}
			}
		}
		else {
			System.out.println(direction + " is not a valid direction.");
		}

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				Tile current = board[row][col];
				if (current == null) continue;
				current.setCanCombine(true);
			}
		}

		if (canMove) {
			audio.play("click", 0);
			spawnRandom();
                        // check if dead 
			setDead(checkDead());
		}
	}
	/**
         * check if the game has lost return of true or false
         * @return 
         */
	private boolean checkDead() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				if (board[row][col] == null) return false;
				boolean canCombine = checkSurroundingTiles(row, col, board[row][col]);
				if (canCombine) {
					return false;
				}
			}
		}
		return true; 
	}
	/**
         * check if the game has won 
         * @return 
         */
	private boolean checkWon() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				if(board[row][col] == null) continue;
				if(board[row][col].getValue() >= 2048) return true;
			}
		}
		return false;
	}
        /**
         * TRUE OR False if its posible to move any sides
         * @param row
         * @param col
         * @param tile
         * @return 
         */
	private boolean checkSurroundingTiles(int row, int col, Tile tile) {
		if (row > 0) {      // left
			Tile check = board[row - 1][col];
			if (check == null) return true; // can combine
			if (tile.getValue() == check.getValue()) return true;
		}
		if (row < ROWS - 1) { // right
			Tile check = board[row + 1][col];
			if (check == null) return true;
			if (tile.getValue() == check.getValue()) return true;
		}
		if (col > 0) {      // up
			Tile check = board[row][col - 1];
			if (check == null) return true;
			if (tile.getValue() == check.getValue()) return true;
		}
		if (col < COLS - 1) {//down
			Tile check = board[row][col + 1];
			if (check == null) return true;
			if (tile.getValue() == check.getValue()) return true;
		}
		return false;
	}
        /**
         * randomly spawn in either rows or columns-
         */
	private void spawnRandom() { 
		Random random = new Random();
		boolean notValid = true;

		while (notValid) {
			int location = random.nextInt(16);
			int row = location / ROWS;
			int col = location % COLS;
			Tile current = board[row][col];
			if (current == null) {
				int value = random.nextInt(10) < 9 ? 2 : 4;
				Tile tile = new Tile(value, getTileX(col), getTileY(row));
				board[row][col] = tile;
				notValid = false;
			}
		}
	}
        /**
         * check if the tiles available to move either sides 
         */
	private void checkKeys() {
		if (!Keys.pressed[KeyEvent.VK_LEFT] && Keys.prev[KeyEvent.VK_LEFT]) {
			moveTiles(LEFT); // move tiles left
			if (!hasStarted) hasStarted = !dead;
		}
		if (!Keys.pressed[KeyEvent.VK_RIGHT] && Keys.prev[KeyEvent.VK_RIGHT]) {
			moveTiles(RIGHT);// move tiles right
			if (!hasStarted) hasStarted = !dead;
		}
		if (!Keys.pressed[KeyEvent.VK_UP] && Keys.prev[KeyEvent.VK_UP]) {
			moveTiles(UP);// move tiles up
			if (!hasStarted) hasStarted = !dead;
		}
		if (!Keys.pressed[KeyEvent.VK_DOWN] && Keys.prev[KeyEvent.VK_DOWN]) {
			moveTiles(DOWN);// move tiles down
			if (!hasStarted) hasStarted = !dead;
		}
	}
        /**
         * aquire the highest valued tile 
         * @return 
         */
	public int getHighestTileValue(){
		int value = 2;
		for(int row = 0; row < ROWS; row++){
			for(int col = 0; col < COLS; col++){
				if(board[row][col] == null) continue;
				if(board[row][col].getValue() > value) value = board[row][col].getValue();
			}
		}
		return value;
	}
	/**
         * true or false if user lost the game-
         * @return 
         */
	public boolean isDead() {
		return dead;
	}
        /**
         * check if the user has lost the game
         * @param dead 
         */
	public void setDead(boolean dead) {
		if(!this.dead && dead){
			lBoard.addTile(getHighestTileValue());
			lBoard.addScore(scores.getCurrentScore());
			lBoard.saveScores();
		}
		this.dead = dead;
	}
        /**
         * getBoard getter and setter
         * @return 
         */
	public Tile[][] getBoard() {
		return board;
	}	
	public void setBoard(Tile[][] board) {
		this.board = board;
	}
        /**
         *  X axis getter and setter 
         * @return 
         */
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
        /**
         * Y axis getter and setter 
         * @return 
         */
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
        /**
         * true or false if user has won 
         * @return 
         */
	public boolean isWon() {
		return won;
	}
        /**
         * check if user won the game
         * @param won 
         */
	public void setWon(boolean won) {
		if(!this.won && won && !dead){ 
			lBoard.addTime(scores.getTime());
			lBoard.saveScores();
		}
		this.won = won;
	}	
        /**
         * aquire the score during the game
         * @return 
         */
	public ScoreManager getScores(){
		return scores;
	}
}
