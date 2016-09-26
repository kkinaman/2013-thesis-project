package thesis;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * 
 * @author Karina Kinaman
 *
 * The main display component that holds the terrain, food, and vehicles.
 */
public class Field extends Component{
	
	private static final long serialVersionUID = 1L;

	private final Simulator simulator; //the instance of the simulator holding this field
	
	private List<Vehicle> vehicleList = new ArrayList<Vehicle>(); //List of vehicles on the field
	private List<Food> energyFoodList = new ArrayList<Food>(); //List of food (for energy vehicles) on the field
	private List<Food> distanceFoodList = new ArrayList<Food>(); //List of food (for distance vehicles) on the field
	private List<Terrain> terrainList = new ArrayList<Terrain>(); //List of terrain patches
	private List<Food> eatenFoodList = new ArrayList<Food>(); //List of eaten food
	
	private int chosenVehicleNo, chosenDistanceFoodNo, chosenEnergyFoodNo, chosenTerrainNo; //Keeps track of which (if any) vehicle, food, or terrain is selected
	private int deadVehicleCount = 0; //Count of number of dead vehicles
	
	/**
	 * Constructor
	 */
	public Field(Simulator sim){
		this.simulator = sim;
		clearSelection();
		
		this.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
				Point point = e.getPoint();
				clearSelection();
				boolean found = false;
				
				//clicked on vehicle
				if (!found){
					for (int i=0; i < vehicleList.size(); i++) {
						Vehicle curVehicle = vehicleList.get(i);
						if (curVehicle.getBody().contains(point)){
							chosenVehicleNo = i;
							found = true;
							break;
						}
					}
				}
				//clicked on distance food
				if (!found){
					for (int i=0; i < distanceFoodList.size(); i++) {
						Food curFood = distanceFoodList.get(i);
						if (curFood.getArea().contains(point)){
							chosenDistanceFoodNo = i;
							found = true;
							break;
						}
					}
				}
				//clicked on energy food
				if (!found){
					for (int i=0; i < energyFoodList.size(); i++) {
						Food curFood = energyFoodList.get(i);
						if (curFood.getArea().contains(point)){
							chosenEnergyFoodNo = i;
							found = true;
							break;
						}
					}
				}
				//clicked on terrain
				if (!found){
					for (int i = 0; i < terrainList.size(); i++){
						Terrain curTerrain = terrainList.get(i);
						if (curTerrain.getCircle().contains(point)){
							chosenTerrainNo = i;
							found = true;
							break;
						}
					}
				}
				repaint();
			}
		});
		
		this.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				if (chosenVehicleNo >= 0){
					Vehicle chosenVehicle = vehicleList.get(chosenVehicleNo);
					chosenVehicle.resetPos(e.getX(), e.getY());
				}
				if (chosenDistanceFoodNo >= 0){
					Food chosenFood = distanceFoodList.get(chosenDistanceFoodNo);
					chosenFood.resetPos(e.getX(), e.getY());
				}
				if (chosenEnergyFoodNo >= 0){
					Food chosenFood = energyFoodList.get(chosenEnergyFoodNo);
					chosenFood.resetPos(e.getX(), e.getY());
				}
				if (chosenTerrainNo >= 0){
					Terrain chosenTerrain = terrainList.get(chosenTerrainNo);
					chosenTerrain.resetPos(e.getX(), e.getY());
				}
				repaint();
			}
		});
	}
	
	public void paint(Graphics g){
		update(g);
	}
	
	public void update(Graphics g){
		
		//double buffering
		Image offImage = createImage (this.getSize().width, this.getSize().height);
		Graphics offG = offImage.getGraphics();
		
		// clear screen in background 
		offG.setColor(Simulator.WINDOW_COLOR); 
		offG.fillRect (0, 0, this.getSize().width, this.getSize().height); 
		
		//draw terrain patches
		for (int i = 0; i < terrainList.size(); i++){
			terrainList.get(i).paint(offG);
		}
		
		//draw circle around chosen terrain
		if (chosenTerrainNo >= 0) {
			Terrain chosenTerrain = terrainList.get(chosenTerrainNo);
			offG.setColor(Color.black);
			Graphics2D g2 = (Graphics2D) offG;
			g2.draw(chosenTerrain.getCircle());
		}
		
		//draw circle around chosen food
		if (chosenDistanceFoodNo >=0 || chosenEnergyFoodNo >=0){
			Food chosenFood = new Food();
			if (chosenDistanceFoodNo >= 0) chosenFood = distanceFoodList.get(chosenDistanceFoodNo);
			else if (chosenEnergyFoodNo >=0) chosenFood = energyFoodList.get(chosenEnergyFoodNo);
			offG.setColor(Color.magenta);
			offG.drawOval((int)(chosenFood.getCenterX() - 25),(int)(chosenFood.getCenterY() - 25), 50, 50);
		}
		
		//draw distance food
		BufferedImage img = null;
		try {
			img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(
					Food.pathnames[Food.DISTANCE]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < distanceFoodList.size(); i++){
			distanceFoodList.get(i).paint(offG, img);
		}
		
		//draw energy food
		try {
			img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(
					Food.pathnames[Food.ENERGY]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < energyFoodList.size(); i++){
			energyFoodList.get(i).paint(offG, img);
		}
		
		//draw eaten food
		BufferedImage img_d = null;
		BufferedImage img_e = null;
		try {
			img_d = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(
					Food.pathnames[Food.DISTANCE_EATEN]));
			img_e = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(
					Food.pathnames[Food.ENERGY_EATEN]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (simulator.isShowEatenFood()){
			for (int i = 0; i < eatenFoodList.size(); i++){
				if (eatenFoodList.get(i).getType()==0)
				eatenFoodList.get(i).paint(offG, img_d);
				else eatenFoodList.get(i).paint(offG, img_e);
			}
		}
		
		//draw circle around chosen vehicle
		if (chosenVehicleNo >= 0) {
			Vehicle chosenVehicle = vehicleList.get(chosenVehicleNo);
			offG.setColor(Color.cyan);
			offG.drawOval((int)chosenVehicle.getCenterX() - chosenVehicle.getVehicleSize()/2, 
					(int)chosenVehicle.getCenterY() - chosenVehicle.getVehicleSize()/2, 
					chosenVehicle.getVehicleSize(), chosenVehicle.getVehicleSize());
		}

		//draw vehicles
		for (int i = 0; i < vehicleList.size(); i++){
			vehicleList.get(i).paint(offG);
		}	
		
		// draw image from buffer onto the screen 
		g.drawImage (offImage, 0, 0, this); 
		
		//garbage colleciton
		offG.dispose();
		offImage.flush();
	}
	
	/**
	 * When triggered by foodTimer in Simulator.java, adds food to field (amount specified in Simulator.java -> FOOD_DIST_AMOUNT)
	 */
	public void foodDrop(){
		for (int i = 0; i < simulator.getFoodRefillAmount(); i++){
			simulator.addFood();
		}
	}
	
	/**
	 * Test whether all vehicles have died
	 * @return True if all vehicles have died
	 */
	public boolean allDead(){
		if (deadVehicleCount == vehicleList.size()) return true;
		else return false;
	}
	
	public void clearSelection(){
		chosenVehicleNo = -1;
		chosenEnergyFoodNo = -1;
		chosenDistanceFoodNo = -1;
		chosenTerrainNo = -1;
	}
	
	public List<Vehicle> getVehicleList() {
		return vehicleList;
	}
	public List<Food> getEnergyFoodList() {
		return energyFoodList;
	}
	public List<Food> getDistanceFoodList() {
		return distanceFoodList;
	}
	public List<Terrain> getTerrainList() {
		return terrainList;
	}
	public List<Food> getEatenFoodList() {
		return eatenFoodList;
	}
	
	public int getChosenVehicleNo() {
		return chosenVehicleNo;
	}
	public int getChosenDistanceFoodNo() {
		return chosenDistanceFoodNo;
	}
	public int getChosenEnergyFoodNo() {
		return chosenEnergyFoodNo;
	}
	public int getChosenTerrainNo() {
		return chosenTerrainNo;
	}

	public void setChosenVehicleNo(int chosenVehicleNo) {
		this.chosenVehicleNo = chosenVehicleNo;
	}
	public void setChosenDistanceFoodNo(int chosenDistanceFoodNo) {
		this.chosenDistanceFoodNo = chosenDistanceFoodNo;
	}
	public void setChosenEnergyFoodNo(int chosenEnergyFoodNo) {
		this.chosenEnergyFoodNo = chosenEnergyFoodNo;
	}
	public void setChosenTerrainNo(int chosenTerrainNo) {
		this.chosenTerrainNo = chosenTerrainNo;
	}

	public int getDeadVehicleCount() {
		return deadVehicleCount;
	}

	public void setDeadVehicleCount(int deadVehicleCount) {
		this.deadVehicleCount = deadVehicleCount;
	}

	public Simulator getSimulator() {
		return simulator;
	}

}
