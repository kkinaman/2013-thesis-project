package thesis;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Karina Kinaman
 *
 * Vehicle objects displayed on Field.
 */
public class Vehicle{
	
	private Field field; //Instance of the field on which the vehicle exists
	
	private Polygon body = new Polygon(); //Vehicle body--Polygon type because it needs to be a rectangle that can be drawn at any rotation
	private double centerX, centerY; //Coordinates of center of vehicle body
	private double positionX, positionY, orientInRadians; //Position of left sensor and orientation of vehicle--used for positioning
	private int bodyWidth, bodyHeight, wheelWidth, wheelHeight, sensorDiam; //Vehicle proportions
	private double speed, energy; //Vehicle speed and energy level
	private int vehicleType; //Type of vehicle: 0-distance, 1-energy
	private int vehicleSize; //Relative size of vehicle
	
	private List<QuadCurve2D> connections = new ArrayList<QuadCurve2D>();  //Vehicle sensor-wheel connections
	private Point2D[] sensorPositions = new Point2D[2]; //Center points of left (0) and right (1) sensors
	private Point2D[] wheelPositions = new Point2D[2]; //Center points of left (0) and right (1) wheels
	private Point2D[] bodyCorners = new Point2D[4]; //Corner points of rectangle body (top-left(0), top-right(1), bottom-right(2), bottom_left(3))
	private EnergyBar energyBar; //Progress bar showing vehicle's energy level
	
	private Food targetFood; //The targeted piece of food
	private boolean deceased = false; //Whether a vehicle has died
	private List<Terrain> terrainToCross = new ArrayList<Terrain>(); //List of which terrains the vehicle plans to cross
	
	private final static int FOOD_CONN_LEFT = 0;
	private final static int FOOD_CONN_RIGHT = 1;
	private final static int TERR_CONN_LEFT = 2;
	private final static int TERR_CONN_RIGHT = 3;
	
	private final static int LEFT = 0; //Constant for the index of the left component
	private final static int RIGHT = 1; //Constant for the index of the left component
	
	private final static Color[] VEHICLE_COLOR = {Color.orange, Color.blue}; //Color of vehicle body: distance-orange, energy-blue
	private final static Color WHEEL_COLOR = Color.darkGray; //Color of vehicle wheels, sensors, and connections
	private final static double DEFAULT_SPEED = 0.05; //Default speed - relative to how far (pixels) the vehicle moves per step
	private final static int INITIAL_ENERGY = 50; //Amount of energy the vehicle starts with (0-100)
	private final static int PATH_POINT_INTERVAL = 100; //record the point the vehicle is at every 100th step
	
	private List<Point2D> pathPoints = new ArrayList<Point2D>();
	private int stepCounter = 0;
	private boolean recordPoint = false;
	
	private final static int DISTANCE = 0; //Constant for distance type
	private final static int ENERGY = 1; //Constant for energy type

		
	/**
	 * Constructor: Sets proportions of vehicle elements
	 * @param x	x-coordinate of center of left sensor
	 * @param y	y-coordinate of center of left sensor
	 * @param orient	orientation of vehicle
	 * @param vehicleType	type of vehicle: 0-distance, 1-energy
	 * @param field	instance of the playing field
	 * @param size	size of the vehicle
	 */
	public Vehicle(double x, double y, double orient, int vehicleType, Field field, int size){
		this.positionX = x;
		this.positionY = y;
		this.orientInRadians = orient;
		this.vehicleType = vehicleType;
		this.field = field;
		this.vehicleSize = size;
		setConnections();
		
		speed = DEFAULT_SPEED;
		energy = INITIAL_ENERGY;
		//float the energy bar slightly above the left corner of the vehicle
		energyBar = new EnergyBar(positionX - vehicleSize/3, positionY - vehicleSize/3, energy);
		
		//set dimensions of vehicle parts relative to size
		bodyWidth = (int)(vehicleSize*.5);
		bodyHeight = (int)(vehicleSize*.75);
		wheelWidth = (int)(vehicleSize*.2);
		wheelHeight = (int)(vehicleSize*.2);
		sensorDiam = (int)(vehicleSize*.1);
		
		//update positions of vehicle parts
		updateSensors();
		updateWheels();
		updateBody();
		updateConnections();
	}
	
	/**
	 * Sets connections depending on vehicle type
	 */
	private void setConnections(){
		connections.add(new QuadCurve2D.Double());
		connections.add(new QuadCurve2D.Double());
		if (vehicleType==1) { //energy vehicles have an extra set of connections for detecting terrain
			connections.add(new QuadCurve2D.Double());
			connections.add(new QuadCurve2D.Double());
		}
	}

	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		if (field.getSimulator().showPaths()){
			g2.setColor(VEHICLE_COLOR[vehicleType]);
			//draw path traveled
			drawPaths(g2);
		}
		
		//Only want to draw vehicles who are still living
		if (!deceased){ 
			//draw body of vehicle: make arrays of x- and y- coords for Polygon type
			int[] bodyX = new int[4];
			int[] bodyY = new int[4];
			for (int i = 0; i < bodyCorners.length; i++){
				bodyX[i] = (int)bodyCorners[i].getX();
				bodyY[i] = (int)bodyCorners[i].getY();
			}
			g2.setColor(VEHICLE_COLOR[vehicleType]);
			body = new Polygon(bodyX, bodyY, 4);
			g2.fill(body);			
	
			//draw wheels
			g2.setColor(WHEEL_COLOR);
			g.fillOval((int)wheelPositions[LEFT].getX() - wheelWidth/2, (int)wheelPositions[LEFT].getY() - wheelHeight/2, wheelWidth, wheelHeight);
			g.fillOval((int)wheelPositions[RIGHT].getX() - wheelWidth/2, (int)wheelPositions[RIGHT].getY() - wheelHeight/2, wheelWidth, wheelHeight);
	
			//draw sensors
			g.fillOval((int)sensorPositions[LEFT].getX() - sensorDiam/2, (int)sensorPositions[LEFT].getY() - sensorDiam/2, sensorDiam, sensorDiam);
			g.fillOval((int)sensorPositions[RIGHT].getX() - sensorDiam/2, (int)sensorPositions[RIGHT].getY() - sensorDiam/2, sensorDiam, sensorDiam);
	
			//draw connections
			for (int i = 0; i < connections.size(); i++){
				g2.draw(connections.get(i));
			}
	
			//reset position of progress bar
			energyBar.setPos(positionX - vehicleSize/3, positionY - vehicleSize/3);
			energyBar.paint(g);
		}
	}
	
	/**
	 * Updates a vehicle's sensors
	 */
	public void updateSensors(){
		sensorPositions[LEFT] = new Point2D.Double(positionX, positionY);
		sensorPositions[RIGHT] = new Point2D.Double(positionX + bodyWidth*Math.cos(orientInRadians), positionY - bodyWidth*Math.sin(orientInRadians));
	}
	
	/**
	 * Updates a vehicle's wheels
	 */
	public void updateWheels(){
		wheelPositions[LEFT] = new Point2D.Double((sensorPositions[LEFT].getX() + bodyHeight*Math.sin(orientInRadians)), sensorPositions[LEFT].getY() + bodyHeight*Math.cos(orientInRadians));
		wheelPositions[RIGHT] = new Point2D.Double(wheelPositions[LEFT].getX() + bodyWidth*Math.cos(orientInRadians), wheelPositions[LEFT].getY() - bodyWidth*Math.sin(orientInRadians));
	}
	
	/**
	 * Updates the position of the vehicle body
	 */
	public void updateBody(){
		bodyCorners[0] = new Point2D.Double(sensorPositions[LEFT].getX(), sensorPositions[LEFT].getY());
		bodyCorners[1] = new Point2D.Double(sensorPositions[RIGHT].getX(), sensorPositions[RIGHT].getY());
		bodyCorners[2] = new Point2D.Double(wheelPositions[RIGHT].getX(), wheelPositions[RIGHT].getY());
		bodyCorners[3] = new Point2D.Double(wheelPositions[LEFT].getX(), wheelPositions[LEFT].getY());
	}
	
	/**
	 * Updates the sensor-wheel connections
	 */
	public void updateConnections(){
		centerX = (((bodyCorners[0].getX()+bodyCorners[3].getX())/2) + ((bodyCorners[1].getX()+bodyCorners[2].getX())/2))/2;
		centerY = (((bodyCorners[0].getY()+bodyCorners[3].getY())/2) + ((bodyCorners[1].getY()+bodyCorners[2].getY())/2))/2;
		//connect left sensor to left wheel
		connections.get(FOOD_CONN_LEFT).setCurve(sensorPositions[LEFT].getX(), sensorPositions[LEFT].getY(),
				centerX, centerY, wheelPositions[LEFT].getX(), wheelPositions[LEFT].getY());
		//connect right sensor to right wheel
		connections.get(FOOD_CONN_RIGHT).setCurve(sensorPositions[RIGHT].getX(), sensorPositions[RIGHT].getY(),
				centerX, centerY, wheelPositions[RIGHT].getX(), wheelPositions[RIGHT].getY());
		if (vehicleType==1){ //if an energy robot, update terrain connections as well
			//connect left sensor to right wheel
			connections.get(TERR_CONN_LEFT).setCurve(sensorPositions[LEFT].getX(), sensorPositions[LEFT].getY(), 
					centerX, centerY, wheelPositions[RIGHT].getX(), wheelPositions[RIGHT].getY());
			//connect right sensor to left wheel
			connections.get(TERR_CONN_RIGHT).setCurve(sensorPositions[RIGHT].getX(), sensorPositions[RIGHT].getY(), 
					centerX, centerY, wheelPositions[LEFT].getX(), wheelPositions[LEFT].getY());
		}
	}
	
	private void drawPaths(Graphics2D g2){
		if (pathPoints.size()>=2){ //need at least two points to draw a line
			for (int i = 0; i < pathPoints.size()-1; i++){
					g2.draw(new Line2D.Double(pathPoints.get(i).getX(), pathPoints.get(i).getY(), 
							pathPoints.get(i+1).getX(), pathPoints.get(i+1).getY()));
			}
		}
	}
	
	/**
	 * Steps the vehicle forward one step
	 */
	public void step(){
		
		//if the vehicle has not died, continue
		if (!checkForDeath()){
			//update position from last step
			updateSensors();
			updateWheels();
			updateBody();
			updateConnections();
	
			//if the vehicle has intersected its target food, eat it
			if (targetFood!=null){
				if (body.intersects(targetFood.getArea())) eat(targetFood);
			}	
	
			//if the target food has not been chosen or has been eaten, pick a piece of food
			if (targetFood == null || field.getEatenFoodList().contains(targetFood)){
				setTargetFood();
			}
						
			//determine the speed of the wheels
			double lspeed = calculateLeftWheelSpeed(); 
			double rspeed = calculateRightWheelSpeed();
	
			//variables to track changes in position
			double dx = 0;
			double dy = 0;
			double ds, dorient;
			double orientInDegrees = Math.toDegrees(orientInRadians);
	
			/*Step the vehicle forward 'speed' pixels and update positions
				Direction of movement depends on the vehicle's orientation */
			if((orientInDegrees%360)<=90){ //facing top right
				dx = Math.cos(Math.toRadians(90-orientInDegrees))*((lspeed+rspeed)/2)*speed;
				dy = Math.sin(Math.toRadians(90-orientInDegrees))*((lspeed+rspeed)/2)*speed;
				positionX = positionX - dx;
				positionY = positionY - dy;
			}
			else if ((orientInDegrees%360)>90 && (orientInDegrees%360)<=180){ //facing top left
				dx = Math.cos(Math.toRadians(90-(180-orientInDegrees)))*((lspeed+rspeed)/2)*speed;
				dy = Math.sin(Math.toRadians(90-(180-orientInDegrees)))*((lspeed+rspeed)/2)*speed;
				positionX = positionX - dx;
				positionY = positionY + dy;
			}
			else if ((orientInDegrees%360)>180 && (orientInDegrees%360)<=270){ //facing bottom left
				dx = Math.cos(Math.toRadians(360-(orientInDegrees+90)))*((lspeed+rspeed)/2)*speed;
				dy = Math.sin(Math.toRadians(360-(orientInDegrees+90)))*((lspeed+rspeed)/2)*speed;
				positionX = positionX + dx;
				positionY = positionY + dy;
			}
			else if ((orientInDegrees%360)>270 && (orientInDegrees%360)<=360){ //facing bottom right
				dx = Math.cos(Math.toRadians(90-(360-orientInDegrees)))*((lspeed+rspeed)/2)*speed;
				dy = Math.sin(Math.toRadians(90-(360-orientInDegrees)))*((lspeed+rspeed)/2)*speed;
				positionX = positionX + dx;
				positionY = positionY - dy;
			}
			
			//Too many points to record every step; only record every 100th
			stepCounter++;
			if (stepCounter>=PATH_POINT_INTERVAL){
				stepCounter = 0;
				recordPoint = true;
			}
			if (recordPoint){
				//add new location to drawn path
				pathPoints.add(new Point2D.Double(positionX, positionY));
				recordPoint = false;
			}
			
			//determine how much to rotate vehicle based on differences of two wheel speeds
			//triple to increase turning radius
			ds = Math.abs(lspeed-rspeed)*speed;
			dorient = Math.atan(ds/bodyWidth)*3; 
	
			//reorient the vehicle
			if (rspeed > lspeed){
				orientInRadians = orientInRadians + dorient;
			}
			else if (lspeed > rspeed){
				orientInRadians = orientInRadians - dorient;
			}
			
			//if the robot goes out of bounds, face it back into the field
			if (!body.intersects(new Rectangle2D.Double(0,0,Simulator.WINDOW_WIDTH, Simulator.WINDOW_HEIGHT))){
	
				if (positionX <= 0){
					positionX = 0;
					orientInRadians = (3*Math.PI)/2;
				}
				if (positionX >= Simulator.WINDOW_WIDTH){
					positionX = Simulator.WINDOW_WIDTH;
					orientInRadians = Math.PI/2;
				}
				if (positionY <= 0){
					positionY = 0;
					orientInRadians = Math.PI;
				}
				if (positionY >= Simulator.WINDOW_HEIGHT){
					positionY = Simulator.WINDOW_HEIGHT;
					orientInRadians = 0;
				}
			}
			
			//decrease vehicle's energy level
			double distanceTraveled = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			if (distanceTraveled==0){
				//When moving, the vehicles tend to use anywhere from 1e-6 to 9e-5 units of energy every step
				//So when idle, the vehicles will use 5e-6 units per step
				energy -= 0.000005;
				energyBar.setValue((int) energy); //reset energy bar
			}
			else{
				List<Terrain> terrainList = field.getTerrainList();
				int terrainCost = 1;
				//if they are over a terrain patch, set the terrain cost appropriately
				outerloop: for (int i = 0; i < terrainList.size(); i++){
					Ellipse2D terrain = terrainList.get(i).getCircle();
					for (int j = 0; j < bodyCorners.length; j++){
						if (terrain.contains(bodyCorners[j].getX(), bodyCorners[j].getY())){
							terrainCost = terrainList.get(i).getTerrainCost();
							break outerloop;
						}
					}
				}
				energy -= calculateEnergyExpenditure(distanceTraveled, terrainCost); //decrement energy spent
				energyBar.setValue((int) energy); //update energy bar
			}
		}
	}
	
	/**
	 * Checks if the vehicle has died and handles death procedure
	 * @return True if vehicle has died, False if it is still living
	 */
	private boolean checkForDeath(){
		if (energy <= 0){ //if vehicle has died
			speed = 0;
			deceased = true;
			field.setDeadVehicleCount(field.getDeadVehicleCount()+1); //increment the count of dead vehicles
			
			//add vehicle to cemetery
			if (vehicleType==DISTANCE) { //if distance
				field.getSimulator().addDistanceToCemetery();
			}
			if (vehicleType==ENERGY) { //if energy
				field.getSimulator().addEnergyToCemetery();
			}
			
			//if box is checked to automatically add a new vehicle upon death, add one
			if (field.getSimulator().isAddNewVehicles()){ 
				field.getVehicleList().add(field.getSimulator().generateNewVehicle(vehicleType, field, vehicleSize));
				field.repaint();
			}
			
			//if all vehicles have died, pause the simulator
			if (field.allDead()){
				field.getSimulator().allDeadPause();
			}
			
			return true;
		}
		else return false;
	}
	
	
	/**
	 * Based on the type of vehicle, finds the food of lowest cost and sets it as target
	 */
	public void setTargetFood(){
		Food target = null;
		double netGain = 0;
		double largestGain = 0;
		List<Food> foodList = new ArrayList<Food>();
		List<Terrain> terrainList = field.getTerrainList();
		
		//Distance-scaling: find the food closest to the robot (highest net gain of value of food - distance to travel)
		if (vehicleType == DISTANCE){
			foodList = field.getDistanceFoodList();
			for(int i = 0; i < foodList.size(); i++){ 
				Food curFood = foodList.get(i);
				Point2D vehicleLoc = new Point2D.Double(centerX, centerY);
				Point2D foodLoc = new Point2D.Double(curFood.getX(), curFood.getY());
				double distVehicleToFood = vehicleLoc.distance(foodLoc);
				//calculate net gain, note that distance vehicles always assume flat ground
				netGain = curFood.getValue() - calculateEnergyExpenditure(distVehicleToFood, Terrain.FLAT_TERRAIN_COST);
				if (netGain > largestGain){ 
					largestGain = netGain;
					target = curFood;
				}
			}
		}
		//Bioenergetic-scaling: find food that will take minimum energy to get to (highest net gain of value of food - energy spent)
		else if (vehicleType == ENERGY){
			foodList = field.getEnergyFoodList();
			for(int i = 0; i < foodList.size(); i++){
				Food curFood = foodList.get(i);
				List<Terrain> tempTerrainToCross = new ArrayList<Terrain>();
				Point2D vehicleLoc = new Point2D.Double(centerX, centerY);
				Point2D foodLoc = new Point2D.Double(curFood.getCenterX(), curFood.getCenterY());
				double straightPathDistance = vehicleLoc.distance(foodLoc);
				double pathDistance = straightPathDistance; //initialize pathDistance to the straight-line distance to the food
				double pathCost = calculateEnergyExpenditure(pathDistance, Terrain.FLAT_TERRAIN_COST); //initialize pathCost to the cost of traveling the straight path if there were no terrain
				Line2D straightPathLine = new Line2D.Double(vehicleLoc.getX(), vehicleLoc.getY(), foodLoc.getX(), foodLoc.getY());
				for (int j = 0; j < terrainList.size(); j++){
					Terrain curTerr = terrainList.get(j);
					double distStraightPathToTerrain = straightPathLine.ptSegDist(curTerr.getCircle().getCenterX(), curTerr.getCircle().getCenterY());
					double r = curTerr.getRadius();
					if (distStraightPathToTerrain < r) { //if the straight path intersects the terrain
						
						double chordLength = 2*Math.sqrt(Math.pow(r, 2) - Math.pow(distStraightPathToTerrain, 2));
						double theta = 2 * Math.asin(chordLength/(2*r));
						double arcLength = r * theta;
						int terrainCost = curTerr.getTerrainCost();
						if (curTerr.contains(curFood)){ //if the terrain contains the food, they will have to cross it
							pathCost -= calculateEnergyExpenditure(chordLength, Terrain.FLAT_TERRAIN_COST); //subtract assumed cost of crossing terrain
							pathCost += calculateEnergyExpenditure(chordLength, terrainCost); //add the real cost of crossing the terrain
							tempTerrainToCross.add(curTerr);
						}
						else{
							//if it costs more energy to cross the terrain than go around it, then plan to go around
							if (calculateEnergyExpenditure(chordLength, terrainCost) > calculateEnergyExpenditure(arcLength, Terrain.FLAT_TERRAIN_COST)){
								pathDistance -= chordLength; //subtract assumed distance to cross terrain
								pathDistance += arcLength; //add distance to go around terrain
								pathCost -= calculateEnergyExpenditure(chordLength, Terrain.FLAT_TERRAIN_COST); //subtract assumed cost of crossing terrain
								pathCost += calculateEnergyExpenditure(arcLength, Terrain.FLAT_TERRAIN_COST); //add the cost of going around terrain (flat ground)
							}		
							else{ //else, plan on crossing it
								//don't need to change pathDistance because we already assumed we'd take the straight line path
								pathCost -= calculateEnergyExpenditure(chordLength, Terrain.FLAT_TERRAIN_COST); //subtract assumed cost of crossing terrain
								pathCost += calculateEnergyExpenditure(chordLength, terrainCost); //add the real cost of crossing the terrain
								tempTerrainToCross.add(curTerr);
							}
						}
					}
				}
				netGain = curFood.getValue() - pathCost;
				if (netGain > largestGain){
					largestGain = netGain;
					target = curFood;
					terrainToCross = tempTerrainToCross;
				}
			}
		}
		targetFood = target;
	}
	
	/**
	 * Calculates how much energy is spent going some distance over some terrain
	 * @param distance	The distance traveled
	 * @param terrainCost	The cost of the terrain crossed
	 * @return	Amount of energy spent
	 */
	public double calculateEnergyExpenditure(double distance, int terrainCost){
		//divide distance value by 200 to keep energy spent comparable to the amount of energy food contains
		return (distance/200)*terrainCost;
	}
	
	/**
	 * Calculates the speed of the left wheel based on the reading from the corresponding sensor(s)
	 * @return	speed to set wheel to
	 */
	public double calculateLeftWheelSpeed(){
		double leftSpeed = 0;
		//Distance-scaling: Wheels get input from same-side food sensor
		if (this.vehicleType==DISTANCE){ 
			double leftFoodReading = getFoodSensorReading(sensorPositions[LEFT]);
			leftSpeed = leftFoodReading/750; //divide by 750 to normalize to a small number of pixels
		}
		//Energy-scaling: Wheels get input from same-side food sensor and opposite-side terrain sensor
		else if (this.vehicleType==ENERGY){ 
			double leftFoodReading = getFoodSensorReading(sensorPositions[LEFT]);
			double rightTerrainReading = getTerrainSensorReading(sensorPositions[RIGHT]);
			leftSpeed = (leftFoodReading + rightTerrainReading) / 750; //divide by 750 to normalize to a small number of pixels
		}
		return leftSpeed;
	}
	
	/**
	 * Calculates the speed of the right wheel based on the reading from the corresponding sensor(s)
	 * @return	speed to set wheel to
	 */
	public double calculateRightWheelSpeed(){
		double rightSpeed = 0;
		//Distance-scaling: Wheels get input from same-side food sensor
		if (this.vehicleType==DISTANCE){ 
			double rightFoodReading = getFoodSensorReading(sensorPositions[RIGHT]);
			rightSpeed = rightFoodReading/750; //divide by 750 to normalize to a small number of pixels
		}
		//Energy-scaling: Wheels get input from same-side food sensor and opposite-side terrain sensor
		else if (this.vehicleType==ENERGY){ 
			double rightFoodReading = getFoodSensorReading(sensorPositions[RIGHT]);
			double leftTerrainReading = getTerrainSensorReading(sensorPositions[LEFT]);
			rightSpeed = (leftTerrainReading + rightFoodReading)/750; //divide by 750 to normalize to a small number of pixels
		}
		return rightSpeed;
	}
	
	/**
	 * Gets a reading from the food sensor
	 * @param sensorPos	The position of the sensor getting the reading
	 * @return	The reading from the sensor of the target food (the distance to the food)
	 */
	public double getFoodSensorReading(Point2D sensorPos){
		double distSensorToFood = 0;
		if (targetFood != null) {
			distSensorToFood = sensorPos.distance(targetFood.getX() ,targetFood.getY());
		}
		return distSensorToFood;
	}
	
	/**
	 * Gets a reading from the terrain sensor
	 * @param sensorPos	The position of the sensor getting the reading
	 * @return
	 */
	public double getTerrainSensorReading(Point2D sensorPos){
		double reading = 0;
		double distance = 0;
		List<Terrain> terrainList = field.getTerrainList();
		for (int i = 0; i < terrainList.size(); i++){
			Terrain curTerr = terrainList.get(i);
			if (!terrainToCross.contains(curTerr)){ //if the terrain is one that it won't cross
				distance = sensorPos.distance(curTerr.getCenterX(), curTerr.getCenterY()) - curTerr.getRadius();
				if (distance < 1) { //if the vehicle is close to the terrain
					reading = distance * 2.5; //double the reading so that it gets a strong reading
					break;
				}
			}
		}	
		return reading; //will return 0 if the vehicle wasn't close to any terrain
	}
	
	/**
	 * When a vehicle intersects a piece of food, it will consume it
	 * @param food	The piece of food that the vehicle will eat
	 */
	public void eat(Food food){
		speed = 0; //stop the vehicle
		
		//add the energy from the food to the vehicle's energy bar
		energy += food.getValue();
		energyBar.setValue(energy);

		field.getEatenFoodList().add(food); //add the food to the list of eaten food
		
		//remove the food from the list of uneaten food
		if (vehicleType==DISTANCE) {
			field.getDistanceFoodList().remove(food);
		}
		else {
			field.getEnergyFoodList().remove(food);
		}
				
		field.repaint();
		
		speed = DEFAULT_SPEED; //restart the vehicle
	}
	
	public void resetPos(double x, double y){
		setPosx(x);
		setPosy(y);
		updateSensors();
		updateWheels();
		updateBody();
		updateConnections();
	}
	
	public double getPosx() {
		return positionX;
	}

	private void setPosx(double posx) {
		this.positionX = posx;
	}

	public double getPosy() {
		return positionY;
	}

	private void setPosy(double posy) {
		this.positionY = posy;
	}

	public Polygon getBody() {
		return body;
	}

	public double getCenterX() {
		return centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public int getBodyWidth() {
		return bodyWidth;
	}

	public int getBodyHeight() {
		return bodyHeight;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public int getVehicleType() {
		return vehicleType;
	}

	public int getVehicleSize() {
		return vehicleSize;
	}

	public boolean isDeceased() {
		return deceased;
	}


}
