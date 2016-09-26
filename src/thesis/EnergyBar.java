package thesis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
/**
 * @author Karina Kinaman
 * 
 * The progress bar that floats above the vehicle.
 * Denotes how much energy the vehicles has remaining (0-100).
 *
 */
public class EnergyBar {

	private double x, y, value; //(x, y) marks the upper left-hand corner of the bar; value 0-100
	private int width, height;
	private final static int MINIMUM_VAL = 0;
	private final static int MAXIMUM_VAL = 100;
	private static final Color BORDER_COLOR = Color.DARK_GRAY;
	private static final Color FILL_COLOR = Color.WHITE;
	private static final Color ZERO_COLOR = Color.RED;
	
	public EnergyBar(double x, double y, double value){
		this.x = x;
		this.y = y;
		
		if (value < MINIMUM_VAL) value = MINIMUM_VAL;
		if (value > MAXIMUM_VAL) value = MAXIMUM_VAL;
		this.value = value;
		
		width = 50;
		height = 5;
	}

	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		if (value > 0) {
			g2.setColor(FILL_COLOR);
			g2.fill(new Rectangle2D.Double(x, y, (value/MAXIMUM_VAL)*width, height));
		}
		else { //right before the vehicle dies, its energy bar will turn red
			g2.setColor(ZERO_COLOR);
			g2.fill(new Rectangle2D.Double(x, y, width, height));
		}
		g2.setColor(BORDER_COLOR);
		g2.draw(new Rectangle2D.Double(x, y, width, height));
	}
	
	/**
	 * Sets the upper-left corner at (x,y)
	 * @param x	
	 * @param y
	 */
	public void setPos(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public void setValue(double value) {
		if (value < MINIMUM_VAL) value = MINIMUM_VAL;
		if (value > MAXIMUM_VAL) value = MAXIMUM_VAL;
		this.value = value;
	}

}
