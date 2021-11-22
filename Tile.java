package gameLogic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * This class creates the tile object and sets the slide speed, size, colour of each value, 
 * as well as sliding positions and conditions
 * @author Avneet and Kayle
 */
public class Tile {

	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;
	public static final int SLIDE_SPEED = 30;
	public static final int ARC_WIDTH = 15;
	public static final int ARC_HEIGHT =15;

	private int value;
	private BufferedImage tileImage;
	private Color background;
	private Color text;
	private Font font;
	private Point slideTo;
	private int x;
	private int y;

	private boolean beginningAnimation = true;
	private double scaleFirst = 0.1;
	private BufferedImage beginningImage;
	
	private boolean combineAnimation = false;
	private double scaleCombine = 1.2;
	private BufferedImage combineImage;
	private boolean canCombine = true;

        /**
         * constructs a tile for the main game board
         * @param value can be 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 0
         * @param x position
         * @param y position
         */
	public Tile(int value, int x, int y) {
		this.value = value;
		this.x = x;
		this.y = y;
		slideTo = new Point(x, y);
		tileImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		beginningImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		combineImage = new BufferedImage(WIDTH * 2, HEIGHT * 2, BufferedImage.TYPE_INT_ARGB);
		drawImage();
	}

        /**
         * 
         */
	public void update() {
		if (beginningAnimation) {
			AffineTransform transform = new AffineTransform();
			transform.translate(WIDTH / 2 - scaleFirst * WIDTH / 2, HEIGHT / 2 - scaleFirst * HEIGHT / 2);
			transform.scale(scaleFirst, scaleFirst);
			Graphics2D g2d = (Graphics2D) beginningImage.getGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.setColor(new Color(0, 0, 0, 100));
			g2d.fillRect(0, 0, WIDTH, HEIGHT);
			g2d.drawImage(tileImage, transform, null);
			scaleFirst += 0.1;
			g2d.dispose();
			if(scaleFirst >= 1) beginningAnimation = false; 
		}
		else if(combineAnimation){
			AffineTransform transform = new AffineTransform();
			transform.translate(WIDTH / 2 - scaleCombine * WIDTH / 2, HEIGHT / 2 - scaleCombine * HEIGHT / 2);
			transform.scale(scaleCombine, scaleCombine);
			Graphics2D g2d = (Graphics2D) combineImage.getGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.setColor(new Color(0, 0, 0, 0));
			g2d.fillRect(0, 0, WIDTH, HEIGHT);
			g2d.drawImage(tileImage, transform, null);
			scaleCombine -= 0.08;
			g2d.dispose();
			if(scaleCombine <= 1) combineAnimation = false;
		}
	}
	
        /**
         * draws on the gui the tile during its beginning, combining and normal phases
         * @param g 
         */
	public void render(Graphics2D g){
		if(beginningAnimation){
			g.drawImage(beginningImage, x, y, null);
		}
		else if(combineAnimation){
			g.drawImage(combineImage, (int)(x + WIDTH / 2 - scaleCombine * WIDTH / 2), (int)(y + HEIGHT / 2 - scaleCombine * HEIGHT / 2), null);
		}
		else{
			g.drawImage(tileImage, x, y, null);
		}
	}
	
        /**
         * colors that will fill in each number tile as well as the color of the number inside the tile
         */
	private void drawImage() {
		Graphics2D g = (Graphics2D) tileImage.getGraphics();
		if (value == 2) {
			background = new Color(0xffffff);
			text = new Color(0x000000);
		}
		else if (value == 4) {
			background = new Color(0xfe8d8d);
			text = new Color(0x000000);
		}
		else if (value == 8) {
			background = new Color(0xff3838);
			text = new Color(0xffffff);
		}
		else if (value == 16) {
			background = new Color(0xe800d8);
			text = new Color(0xffffff);
		}
		else if (value == 32) {
			background = new Color(0x420eff);
			text = new Color(0xffffff);
		}
		else if (value == 64) {
			background = new Color(0x0461ff);
			text = new Color(0xffffff);
		}
		else if (value == 128) {
			background = new Color(0xC5B8D3);
			text = new Color(0x000000);
		}
		else if (value == 256) {
			background = new Color(0x15EBC8);
			text = new Color(0x000000);
		}
		else if (value == 512) {
			background = new Color(0x03BE10);
			text = new Color(0x000000);
		}
		else if (value == 1024) {
			background = new Color(0xf7e12c);
			text = new Color(0x000000);
		}
		else if (value == 2048) {
			background = new Color(0xffe400);
			text = new Color(0xffffff);
		}
		else if(value == 0){
			background = Color.darkGray;
			text = Color.black;
		}
		else{
			background = new Color(0x000000);
			text = new Color(0xffffff);
		}
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		g.setColor(background);
		g.fillRoundRect(0, 0, WIDTH, HEIGHT, ARC_WIDTH, ARC_HEIGHT);

		g.setColor(text);

		if (value <= 64) {
			font = Game.main.deriveFont(36f);
			g.setFont(font);
		}
		else {
			font = Game.main;
			g.setFont(font);
		}

		int drawX = WIDTH / 2 - DrawUtils.getMessageWidth("" + value, font, g) / 2;
		int drawY = HEIGHT / 2 + DrawUtils.getMessageHeight("" + value, font, g) / 2;
		g.drawString("" + value, drawX, drawY);
		g.dispose();
	}

        /**
         * getter and setter for the value in the tile
         * @return 
         */
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		drawImage();
	}

        /**
         * getter and setter that determines where to slide the tiles
         * @return 
         */
	public Point getSlideTo() {
		return slideTo;
	}

	public void setSlideTo(Point slideTo) {
		this.slideTo = slideTo;
	}

        /**
         * locational getters and setters
         * @return 
         */
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
        /**
         * checks if the tiles can combine
         * @param combineAnimation 
         */
	public void setCombineAnimation(boolean combineAnimation){
		this.combineAnimation = combineAnimation;
		if(combineAnimation) scaleCombine = 1.2;
	}
	
        /**
         * starts combine animation by setting it to true or false
         * @return 
         */
	public boolean isCombineAnimation(){
		return combineAnimation;
	}

        /**
         * determines if the tiles can combine
         * @return 
         */
	public boolean canCombine() {
		return canCombine;
	}
        
        /**
         * sets the tile as can or cannot combine (true or false)
         * @param canCombine 
         */
	public void setCanCombine(boolean canCombine) {
		this.canCombine = canCombine;
	}
}
