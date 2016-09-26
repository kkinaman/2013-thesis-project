package thesis;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * 
 * @author Karina Kinaman
 * 
 * Food pieces displayed in Field
 *
 */
public class Food {

	private double x, y; //(x, y) marks upper left-hand corner of food
	private int width, height, value; //width/height for drawing; value marks how big it is & how much energy it contains
	private Rectangle2D area; //A rectangle around the image
	private int type; //Type: either for distance or energy vehicles
	
	final static String[] pathnames = {"carrot.png", "lettuce.png", "orangex.png", "bluex.png"};
	final static int DISTANCE = 0;
	final static int ENERGY = 1;
	final static int DISTANCE_EATEN = 2;
	final static int ENERGY_EATEN = 3;
	
	/**
	 * Default constructor
	 */
	public Food(){
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		value = 0;
		type = 0;
	}
	
	/**
	 * Constructor.
	 * @param x	X-coordinate of food object
	 * @param y	Y-coordinate of food object
	 * @param value	Integer value of food. Determines the width and height of the object (equal to value).
	 * @param type	Type of food. 0-distance; 1-energy
	 */
	public Food(double x, double y, int value, int type){
		//5 pixel drawing boundary around edges so it appears well on-screen
		if (x < 5) 
			x = 5;
		if (x > Simulator.WINDOW_WIDTH - 5) 
			x = Simulator.WINDOW_WIDTH - 5;
		if (y < 5) 
			y = 5;
		if (y > Simulator.WINDOW_HEIGHT - 5) 
			y = Simulator.WINDOW_HEIGHT - 5;
		
		this.x = x;
		this.y = y;
		this.width = value*3; //multiplied to appear more visible
		this.height = value*3;
		this.value = value;
		this.type = type;
		this.area = new Rectangle2D.Double(x, y, width, height);
	}
	
	public void paint(Graphics g, BufferedImage img){
		g.drawImage(img, (int) x, (int) y, width, height, null);

	}
	
	/**
	 * Reset upper left corner at (x,y)
	 */
	public void resetPos(double x, double y){
		setX(x);
		setY(y);
		area = new Rectangle2D.Double(x, y, width, height);
	}
	
	public double getCenterX(){
		return (x + (x + width))/2;
	}
	
	public double getCenterY(){
		return (y + (y + height))/2;
	}

	public double getX() {
		return x;
	}

	private void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	private void setY(double y) {
		this.y = y;
	}

	public int getValue() {
		return value;
	}

	public Rectangle2D getArea() {
		return area;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getType() {
		return type;
	}
	
}
