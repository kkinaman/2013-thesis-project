package thesis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
//import javax.swing.JSlider;
import javax.swing.Timer;

import java.awt.Choice;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Runs the applet
 * @author Karina Kinaman
 *
 */
public class Simulator extends JApplet implements Runnable{
	public Simulator() {
	}

	private static final long serialVersionUID = 1L;

	private Field field; //Component that holds the simulation environment
	
	private volatile Thread animationThread = null; //Pausable thread
	private volatile boolean paused; //Whether the simulation has been paused
	
	private Timer foodTimer; //Timer to distribute food
	private long timeRemaining, lastTimerUpdate; //Keep track of how much time has elapsed
	
	private Random r = new Random(); //To determine random positioning and orientation of new vehicle
	private int foodRefillAmount, foodRefillInterval;//Keeps track of desired amount/interval to refill food by
	private int vehicleSize = SIZE_INIT; //Keeps track of how large the vehicle should be, based on slider position
	private int vehicleType = 0; //Keeps track of which type of vehicle should be generated, based on dropdown menu
	private int terrainType = 0; //Keeps track of which type of terrain should be added, based on dropdown menu
	private int terrainRadius = 0; //Keeps track of the radius to set the terrain to, based on dropdown menu
	private boolean randomRadius = true; //Whether to set random radius for terrain
	private boolean showPaths = false; //Whether to show vehicle paths
	private boolean addNewVehicles = true; //Whether to add a new vehicle when one dies
	private boolean showEatenFood = false; //Whether to show food that has been eaten
	private GridBagConstraints fieldConstraints; //Constraints for field layout
	
	private final static int MAX_FOOD_VALUE = 10; //Maximum value of food
	private final static int MIN_FOOD_VALUE = 5; //Min value of food
//	private final static int SIZE_MIN = 20; //Minimum vehicle size
//	private final static int SIZE_MAX = 80; //Maximum vehicle size
	private final static int SIZE_INIT = 35; //Initial vehicle size
	
	/**
	 * Width of field
	 */
	final static int WINDOW_WIDTH = 700;
	/**
	 * Height of field
	 */
	final static int WINDOW_HEIGHT = 550;
	/**
	 * Background color of field
	 */
	final static Color WINDOW_COLOR = Color.lightGray;

	private JButton btnAddVehicle, btnAddFood, btnAddTerrain, btnRun, btnDelete, btnReset;
	private JLabel labelVehicleChoice, labelFoodInput, labelFoodRefill1, labelFoodRefill2, labelFoodRefill3, 
	labelTerrainChoice, labelTerrainRadius, labelDistanceDead, labelEnergyDead;
	private Choice choiceVehicleType, choiceTerrainType, choiceRadius;
	private JTextField textFieldFood, textFoodRefillAmt, textFoodRefillInterval;
//	private final JLabel labelVehicleSize = new JLabel("Set vehicle size:");
//	private final JSlider sizeSlider = new JSlider(JSlider.HORIZONTAL, SIZE_MIN, SIZE_MAX, SIZE_INIT);
	private JCheckBox pathCheckBox, eatenFoodCheckBox, newVehicleCheckBox;
	private JPanel distanceCemetery, energyCemetery;

	/**
	 * Method for resetting common constraint properties
	 */
	public void resetFieldConstraints(){
		fieldConstraints = new GridBagConstraints();
		fieldConstraints.weightx = 0.5;
		fieldConstraints.gridwidth = 3;
	    fieldConstraints.insets = new Insets(10,5,0,10);
	    fieldConstraints.anchor = GridBagConstraints.WEST;
	}
	
	public void displayGUI(){
		getContentPane().setLayout(new GridBagLayout());
		
		/*~~~~FIELD~~~~*/
		field = new Field(this);
		field.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		field.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		resetFieldConstraints();
		fieldConstraints.gridheight = 16;
		fieldConstraints.gridx = 0;
		fieldConstraints.gridy = 0;
		fieldConstraints.gridwidth = 1;
		fieldConstraints.anchor = GridBagConstraints.NORTH;
		fieldConstraints.insets = new Insets(0,0,0,0);
		getContentPane().add(field, fieldConstraints);
		
//		resetFieldConstraints();
//		fieldConstraints.gridx = 1;
//		fieldConstraints.gridy = 0;
//		fieldConstraints.gridwidth = 2;
//		getContentPane().add(labelVehicleSize, fieldConstraints);
		
//		sizeSlider.setPreferredSize(new Dimension(100, 20));
//		resetFieldConstraints();
//		fieldConstraints.gridx = 2;
//		fieldConstraints.gridy = 0;
//		fieldConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//		getContentPane().add(sizeSlider, fieldConstraints);
//		sizeSlider.addChangeListener(new ChangeListener(){
//			public void stateChanged(ChangeEvent event) {
//				vehicleSize = sizeSlider.getValue();
//			}
//		});
			
		/*~~~~VEHICLE OPTIONS~~~~*/
		labelVehicleChoice = new JLabel("Vehicle type:");
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 0;
		fieldConstraints.insets = new Insets(20,5,0,0);
		getContentPane().add(labelVehicleChoice, fieldConstraints);
		
		choiceVehicleType = new Choice();
		choiceVehicleType.setFocusable(false);
		resetFieldConstraints();
		fieldConstraints.gridx = 2;
		fieldConstraints.gridy = 0;
		getContentPane().add(choiceVehicleType, fieldConstraints);
		choiceVehicleType.add("Distance");
		choiceVehicleType.add("Energy");
		choiceVehicleType.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e)
			{
				String choice=choiceVehicleType.getSelectedItem();
				if (choice.compareTo("Distance")==0){
					vehicleType = 0;
				}
				else if (choice.compareTo("Energy")==0){
					vehicleType = 1;
				}
			}
		});
		
		btnAddVehicle = new JButton("Add Vehicle");
		resetFieldConstraints();
	    fieldConstraints.insets = new Insets(10, 5, 20, 10); 
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 1;
		getContentPane().add(btnAddVehicle, fieldConstraints);
		btnAddVehicle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				field.getVehicleList().add(generateNewVehicle(vehicleType, field, vehicleSize));
				field.repaint();
			}
		});
		
		/*~~~~FOOD OPTIONS~~~~*/
		textFieldFood = new JTextField();
		textFieldFood.setPreferredSize(new Dimension(20, 25));
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 2;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.gridwidth = 1;
		getContentPane().add(textFieldFood, fieldConstraints);
		textFieldFood.setText("1");

		labelFoodInput = new JLabel("pieces to add");
		resetFieldConstraints();
		fieldConstraints.gridx = 2;
		fieldConstraints.gridy = 2;
		getContentPane().add(labelFoodInput, fieldConstraints);
		
		btnAddFood = new JButton("Add Food");
		resetFieldConstraints();
	    fieldConstraints.insets = new Insets(10, 5, 10, 10);  
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 3;
		getContentPane().add(btnAddFood, fieldConstraints);
		btnAddFood.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < Integer.parseInt(textFieldFood.getText()); i++){
					addFood();
				}
				field.repaint();
			}
		});
		
		labelFoodRefill1 = new JLabel("Every");
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 4;
		fieldConstraints.gridwidth = 1;
	    fieldConstraints.insets = new Insets(10, 5, 20, 10);  
	    textFoodRefillAmt = new JTextField();
		getContentPane().add(labelFoodRefill1, fieldConstraints);
		
		textFoodRefillInterval = new JTextField();
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 4;
		fieldConstraints.gridwidth = 1;
		fieldConstraints.anchor = GridBagConstraints.EAST;
	    fieldConstraints.insets = new Insets(10, 5, 20, 10); 
	    textFoodRefillInterval.setPreferredSize(new Dimension(30, 25));
	    textFoodRefillInterval.setText("10");
		getContentPane().add(textFoodRefillInterval, fieldConstraints);
		
		labelFoodRefill2 = new JLabel("seconds, add");
		resetFieldConstraints();
		fieldConstraints.gridx = 2;
		fieldConstraints.gridy = 4;
	    fieldConstraints.insets = new Insets(10, 5, 20, 10);  
		getContentPane().add(labelFoodRefill2, fieldConstraints);
		
		textFoodRefillAmt = new JTextField();
		resetFieldConstraints();
		fieldConstraints.gridx = 3;
		fieldConstraints.gridy = 4;
		fieldConstraints.gridwidth = 1;
		fieldConstraints.anchor = GridBagConstraints.EAST;
	    fieldConstraints.insets = new Insets(10, 5, 20, 10);  
	    textFoodRefillAmt.setPreferredSize(new Dimension(30, 25));
	    textFoodRefillAmt.setText("10");
		getContentPane().add(textFoodRefillAmt, fieldConstraints);
		
		labelFoodRefill3 = new JLabel("pieces");
		resetFieldConstraints();
		fieldConstraints.gridx = 4;
		fieldConstraints.gridy = 4;
		fieldConstraints.gridwidth = 1;
	    fieldConstraints.insets = new Insets(10, 5, 25, 10);  
		getContentPane().add(labelFoodRefill3, fieldConstraints);
		
		/*~~~~TERRAIN OPTIONS~~~~*/
		labelTerrainChoice = new JLabel("Terrain type:");
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 6;
		getContentPane().add(labelTerrainChoice, fieldConstraints);
		
		choiceTerrainType = new Choice();
		choiceTerrainType.setFocusable(false);
		resetFieldConstraints();
		fieldConstraints.gridx = 2;
		fieldConstraints.gridy = 6;
		fieldConstraints.gridwidth = 2;
		getContentPane().add(choiceTerrainType, fieldConstraints);
		choiceTerrainType.add("Fair");
		choiceTerrainType.add("Medium");
		choiceTerrainType.add("Steep");
		choiceTerrainType.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e)
			{
				String choice = choiceTerrainType.getSelectedItem();
				if (choice.compareTo("Fair")==0){
					terrainType = 0;
				}
				else if (choice.compareTo("Medium")==0){
					terrainType = 1;
				}
				else if (choice.compareTo("Steep")==0){
					terrainType = 2;
				}
			}
		});
		
		labelTerrainRadius = new JLabel("Radius:");
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 8;
		fieldConstraints.gridwidth = 1;
		getContentPane().add(labelTerrainRadius, fieldConstraints);
		
		choiceRadius = new Choice();
		choiceRadius.setFocusable(false);
		resetFieldConstraints();
		fieldConstraints.gridx = 2;
		fieldConstraints.gridy = 8;
		fieldConstraints.gridwidth = 2;
		getContentPane().add(choiceRadius, fieldConstraints);
		choiceRadius.add("Random");
		for (int i = 40; i < 100; i=i+5){
			choiceRadius.add(Integer.toString(i));
		}
		choiceRadius.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e)
			{
				String choice = choiceRadius.getSelectedItem();
				if (choice.compareTo("Random") == 0){
					randomRadius = true;
				}
				else {
					randomRadius = false;
					terrainRadius = Integer.parseInt(choice);
				}

			}
		});
		
		btnAddTerrain = new JButton("Add Terrain");
		resetFieldConstraints();
	    fieldConstraints.insets = new Insets(10, 5, 25, 10);
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 9;
		getContentPane().add(btnAddTerrain, fieldConstraints);
		btnAddTerrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double x = r.nextInt(WINDOW_WIDTH);
				double y = r.nextInt(WINDOW_HEIGHT);
				if (randomRadius) terrainRadius = r.nextInt(60) + 40; //random number between 40 and 100
				Terrain newTerrain = new Terrain(x-terrainRadius, y-terrainRadius, terrainRadius, terrainType);
				field.getTerrainList().add(newTerrain);
				field.repaint();
			}
		});
		
		/*~~~~DELETE BUTTON~~~~*/
		btnDelete = new JButton("Delete");
		resetFieldConstraints();
	    fieldConstraints.insets = new Insets(10, 5, 25, 10); 
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 10;
		getContentPane().add(btnDelete, fieldConstraints);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (field.getChosenVehicleNo()>=0){
					field.getVehicleList().remove(field.getChosenVehicleNo());
					field.setChosenVehicleNo(-1);
				}
				if (field.getChosenDistanceFoodNo()>=0){
					field.getDistanceFoodList().remove(field.getChosenDistanceFoodNo());
					field.setChosenDistanceFoodNo(-1);
				}
				if (field.getChosenEnergyFoodNo()>=0){
					field.getEnergyFoodList().remove(field.getChosenEnergyFoodNo());
					field.setChosenEnergyFoodNo(-1);
				}
				if (field.getChosenTerrainNo()>=0){
					 field.getTerrainList().remove(field.getChosenTerrainNo());
					 field.setChosenTerrainNo(-1);
				}
				field.repaint();
			}
		});
		
		/*~~~~RUN/PAUSE BUTTON~~~~*/
		btnRun = new JButton("Run");
		resetFieldConstraints();
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets = new Insets(10, 5, 25, 10); 
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 11;
		btnRun.setBackground(Color.green);
		getContentPane().add(btnRun, fieldConstraints);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (paused == true){ //if the simulator was paused when the button was pressed, start running and change button to allow pausing
					paused = false;
					restartFoodTimer();
					setFoodRefillValues();
					btnRun.setBackground(Color.red);
					disableTools();
					btnRun.setText("Pause");
					field.clearSelection();
				}
				else if (paused==false){ //if the simulator was running when the button was pressed, pause it and change button to allow running
					paused = true;
					pauseFoodTimer();
					btnRun.setBackground(Color.green);
					enableTools();
					btnRun.setText("Run");
				}
			}
		});
		
		/*~~~~CHECKBOX OPTIONS~~~~*/
		pathCheckBox = new JCheckBox("Show Paths", showPaths);
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 12;
		fieldConstraints.insets = new Insets(0, 0, 5, 0); 
		getContentPane().add(pathCheckBox, fieldConstraints);
		pathCheckBox.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent event) {
				showPaths = pathCheckBox.isSelected();
				field.repaint();
			}
		});
		
		eatenFoodCheckBox = new JCheckBox("Show Eaten Food", showEatenFood);
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 13;
		fieldConstraints.insets = new Insets(0, 0, 5, 0);
		getContentPane().add(eatenFoodCheckBox, fieldConstraints);
		eatenFoodCheckBox.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent event){
				showEatenFood = eatenFoodCheckBox.isSelected();
				field.repaint();
			}
		});
		
		newVehicleCheckBox = new JCheckBox("Add New Vehicles Upon Death", addNewVehicles);
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 14;
		fieldConstraints.insets = new Insets(0, 0, 5, 0);
		getContentPane().add(newVehicleCheckBox, fieldConstraints);
		newVehicleCheckBox.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent event){
				addNewVehicles = newVehicleCheckBox.isSelected();
				field.repaint();
			}
		});
		
		/*~~~~RESET BUTTON~~~~*/
		btnReset = new JButton("Reset");
		resetFieldConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 15;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(btnReset, fieldConstraints);
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetField();
				field.repaint();
			}
		});
		
		/*~~~~CEMETERY DISPLAY~~~~*/
		distanceCemetery = new JPanel();
		distanceCemetery.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));
		distanceCemetery.setBackground(Color.white);
		distanceCemetery.setLayout(new FlowLayout(FlowLayout.LEFT));
		resetFieldConstraints();
		fieldConstraints.gridwidth = 7;
		fieldConstraints.gridheight=1;
		fieldConstraints.gridx = 0;
		fieldConstraints.gridy = 15;
		fieldConstraints.insets = new Insets(0,0,0,0);
		getContentPane().add(distanceCemetery, fieldConstraints);
		labelDistanceDead = new JLabel("Distance:");
		distanceCemetery.add(labelDistanceDead);
		
		energyCemetery = new JPanel();
		energyCemetery.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));
		energyCemetery.setBackground(Color.white);
		energyCemetery.setLayout(new FlowLayout(FlowLayout.LEFT));
		resetFieldConstraints();
		fieldConstraints.gridwidth = 7;
		fieldConstraints.gridheight=1;
		fieldConstraints.gridx = 0;
		fieldConstraints.gridy = 16;
		fieldConstraints.insets = new Insets(0,0,0,0);
		getContentPane().add(energyCemetery, fieldConstraints);
		labelEnergyDead = new JLabel("Energy:");
		energyCemetery.add(labelEnergyDead);		
	}
	
	/**
	 * Clears all stored values of Field to clear the graphic display
	 */
	private void resetField(){
		field.getVehicleList().clear();
		field.getTerrainList().clear();
		field.getEatenFoodList().clear();
		field.getEnergyFoodList().clear();
		field.getDistanceFoodList().clear();
		field.setDeadVehicleCount(0);
		distanceCemetery.removeAll();
		distanceCemetery.repaint();
		distanceCemetery.add(labelDistanceDead);
		energyCemetery.removeAll();
		energyCemetery.repaint();
		energyCemetery.add(labelEnergyDead);
	}
	
	/**
	 * Sets amount of and interval at which food should be added, based on GUI values
	 */
	private void setFoodRefillValues(){
		setFoodRefillAmount(Integer.parseInt(textFoodRefillAmt.getText()));
		setFoodRefillInterval(Integer.parseInt(textFoodRefillInterval.getText()));
	}
	
	/**
	 * Renders a new vehicle at a random spot on the field
	 * @param vehicleType	Type: 0-distance; 1-energy
	 * @param field	Field component on which to draw
	 * @param vehicleSize	Relative size of vehicle
	 * @return The generated vehicle
	 */
	public Vehicle generateNewVehicle(int vehicleType, Field field, int vehicleSize){
		double posx = r.nextInt(WINDOW_WIDTH);
		double posy = r.nextInt(WINDOW_HEIGHT);
		double orient = r.nextInt(360);
		orient = Math.toRadians(orient);
		return new Vehicle(posx, posy, orient, vehicleType, field, vehicleSize);
	}
	
	/**
	 * Adds equi-value pieces of distance and energy food
	 */
	public void addFood(){
		double x1 = r.nextInt(WINDOW_WIDTH);
		double y1 = r.nextInt(WINDOW_HEIGHT);
		double x2 = r.nextInt(WINDOW_WIDTH);
		double y2 = r.nextInt(WINDOW_HEIGHT);
		int value = r.nextInt(MAX_FOOD_VALUE - MIN_FOOD_VALUE) + MIN_FOOD_VALUE; //food has value from 5 to 10
		field.getDistanceFoodList().add(new Food(x1, y1, value, Food.DISTANCE));
		field.getEnergyFoodList().add(new Food(x2, y2, value, Food.ENERGY));
	}
	
	/**
	 * Adds a distance vehicle to the cemetery
	 */
	public void addDistanceToCemetery(){
		java.net.URL imgURL = Thread.currentThread().getContextClassLoader().getResource("distance.png");
		ImageIcon icon = new ImageIcon(imgURL);
		Image img = icon.getImage().getScaledInstance(20, 25, Image.SCALE_DEFAULT);
		icon = new ImageIcon(img);
		distanceCemetery.add(new JLabel(icon));
		distanceCemetery.updateUI();
	}
	
	/**
	 * Adds an energy vehicle to the cemetery
	 */
	public void addEnergyToCemetery(){
		java.net.URL imgURL = Thread.currentThread().getContextClassLoader().getResource("energy.png");
		ImageIcon icon = new ImageIcon(imgURL);
		Image img = icon.getImage().getScaledInstance(20, 25, Image.SCALE_DEFAULT);
		icon = new ImageIcon(img);
		energyCemetery.add(new JLabel(icon));
		energyCemetery.updateUI();
	}
	
	public void init(){
		paused=true;
		try {
	        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
	            public void run() {
	                displayGUI();
	            }
	        });
	    } catch (Exception e) {
	    }
		
		timeRemaining = foodRefillInterval*1000; //set food distribution timer to interval time
		//Make a timer that will check every second if a food distribution interval has been reached
		int delay = 3000; 
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) { //this will be evaluated every 3 seconds by foodTimer
				long now = System.currentTimeMillis(); // get current time in milliseconds
			    long elapsed = now - lastTimerUpdate; // get how much time has elapsed since timeRemaining has been updated
			    timeRemaining -= elapsed; // adjust remaining time
			    lastTimerUpdate = now; // remember this update
			    //if we have reached an interval, distribute food and reset interval time
			    if (timeRemaining <= 0){
			    	field.foodDrop();
			    	timeRemaining = foodRefillInterval*1000;
			    }
			}
		};
		foodTimer = new Timer(delay, taskPerformer);

		if (animationThread == null) 
		{
			animationThread = new Thread(this, "Animation");
			animationThread.start();
		}
	}

	@Override
	public void run() {
		Thread curThread = Thread.currentThread();
		while (animationThread == curThread) {
			if (!paused) { //while the simulation is not paused, step each (living) vehicle forward.
				for (int i=0; i < field.getVehicleList().size(); i++) {
					if (!field.getVehicleList().get(i).isDeceased()){
						field.getVehicleList().get(i).step();
						field.repaint();
					}
				}
			}

		}
	}
	
	/**
	 * Method to work with foodTimer to distribute food on set interval. This method pauses the timer which is checking whether the interval has been reached.
	 * It also calculates how much time has elasped since the thread was restarted and subtracts that from the interval time remaining.
	 */
	public void pauseFoodTimer(){
		long now = System.currentTimeMillis();
		long elapsed = now - lastTimerUpdate;
		timeRemaining -= elapsed;
		foodTimer.stop();
	}
	
	/**
	 * Method to work with foodTimer to distribute food on set interval. This method restarts the timer which is checking whether the interval has been reached.
	 * It also notes the time that it was started.
	 */
	public void restartFoodTimer(){
		lastTimerUpdate = System.currentTimeMillis();
		foodTimer.start();
	}
	
	public void disableTools(){
		btnAddVehicle.setEnabled(false);
		btnAddFood.setEnabled(false);
		btnAddTerrain.setEnabled(false);
		btnDelete.setEnabled(false);
		pathCheckBox.setEnabled(false);
//		sizeSlider.setEnabled(false);
		choiceRadius.setEnabled(false);
		choiceVehicleType.setEnabled(false);
		choiceTerrainType.setEnabled(false);
		textFieldFood.setEnabled(false);
		newVehicleCheckBox.setEnabled(false);
		textFoodRefillAmt.setEnabled(false);
		textFoodRefillInterval.setEnabled(false);
		eatenFoodCheckBox.setEnabled(false);
		btnReset.setEnabled(false);
	}
	
	public void enableTools(){
		btnAddVehicle.setEnabled(true);
		btnAddFood.setEnabled(true);
		btnAddTerrain.setEnabled(true);
		btnDelete.setEnabled(true);
		pathCheckBox.setEnabled(true);
//		sizeSlider.setEnabled(true);
		choiceRadius.setEnabled(true);
		choiceVehicleType.setEnabled(true);
		choiceTerrainType.setEnabled(true);
		textFieldFood.setEnabled(true);
		newVehicleCheckBox.setEnabled(true);
		textFoodRefillAmt.setEnabled(true);
		textFoodRefillInterval.setEnabled(true);
		eatenFoodCheckBox.setEnabled(true);
		btnReset.setEnabled(true);
	}
	
	/**
	 * Method called when all vehicles have died, to pause the simulator.
	 */
	public void allDeadPause(){
		paused = true;
		pauseFoodTimer();
		btnRun.setBackground(Color.green);
		enableTools();
		btnRun.setText("Run");
	}

	public boolean showPaths() {
		return showPaths;
	}

	public boolean isAddNewVehicles() {
		return addNewVehicles;
	}

	public int getFoodRefillAmount() {
		return foodRefillAmount;
	}

	private void setFoodRefillAmount(int foodRefillAmount) {
		this.foodRefillAmount = foodRefillAmount;
	}

	private void setFoodRefillInterval(int foodRefillInterval) {
		this.foodRefillInterval = foodRefillInterval;
	}

	public boolean isShowEatenFood() {
		return showEatenFood;
	}

	
}

