package thesis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Karina Kinaman
 *
 * Circles of costly terrain displayed on Field.
 */
public class Terrain {
	private double x, y, radius; //(x,y) marks upper left corner of circle
	private Ellipse2D circle; //the circle drawn
	private RadialGradientPaint gradient; //Colors the circle so not just uniform color
	private Color color;
	
	private final static List<Color> COLOR_INDEX = Arrays.asList(Color.green, Color.yellow, Color.red);
	private final static int[] TERRAIN_COSTS = {10, 15, 20};
	public final static int FLAT_TERRAIN_COST = 1;
	
	/**
	 * Constructor
	 * @param x
	 * @param y
	 * @param radius
	 * @param terrainType	0-green, 1-yellow, 2-red
	 */
	public Terrain(double x, double y, double radius, int terrainType){
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.color = COLOR_INDEX.get(terrainType);
	}
	
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		circle = new Ellipse2D.Double(x, y, radius*2, radius*2);
		float[] dist = {0.0f, 1.0f};
		Color[] colorArray = {color, Simulator.WINDOW_COLOR};
		gradient = new RadialGradientPaint((float)circle.getCenterX(), (float)circle.getCenterY(), (float)(radius*2), dist, colorArray);
		g2.setPaint(gradient);
		g2.fill(circle);
		g2.setPaint(null);
	}
	
	public int getTerrainCost(){
		return TERRAIN_COSTS[COLOR_INDEX.indexOf(color)];
	}
	
	public boolean contains(Food food){
		if (circle.contains(food.getArea()))
			return true;
		return false;
	}
	
	public void resetPos(double x, double y){
		setX(x);
		setY(y);
	}

	public Ellipse2D getCircle() {
		return circle;
	}

	private void setX(double x) {
		this.x = x;
	}

	private void setY(double y) {
		this.y = y;
	}

	public Color getColor() {
		return color;
	}

	public double getRadius() {
		return radius;
	}
	
	public double getCenterX() {
		return circle.getCenterX();
	}
	
	public double getCenterY() {
		return circle.getCenterY();
	}
	
}
