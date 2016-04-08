
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import sensing.ColorSensor;
import utilities.Color;
import utilities.ColorPalette;
import utilities.io.IO;
import utilities.units.Quantity;
import utilities.units.Unit;

/**
 * The main class
 */
public class ION2016 {

	/**
	 * The current debug level. All messages passed to {@link #debug(int, String)} or {@link #debug(int, String, Object...)} will only be printed if
	 * their level is less than or equal to {@link #DEBUG_LEVEL}. Thus, a level of {@link Integer#MAX_VALUE} will cause everything to be printed.
	 */
	public static final int DEBUG_LEVEL = 0;

	/**
	 * Fields:
	 * stopDelay
	 * roadWidth
	 * yellowSpeed
	 * whiteSpeed
	 */
	public static final Map<String, Quantity> courseConstants = new TreeMap<>();
	/**
	 * Fields:
	 * trackWidth
	 * wheelDiameter
	 * sonarGearRatio
	 * sonarMaxAngle
	 * lineFollowingP
	 * lineFollowingI
	 * lineFollowingD
	 * intersectionPullup
	 * parkPullup
	 * parkingSpeed
	 * sonarAlertThreshold
	 * intersectionSweepStart
	 * intersectionSweepEnd
	 * intersectionSweepThreshold
	 * intersectionSweepIncrement
	 * parkingSweepStart
	 * parkingSweepEnd
	 * parkingSweepThreshold
	 * parkingSweepIncrement
	 * parkAvoidBackup
	 * parkAvoidSweepStart
	 * parkAvoidSweepEnd
	 * parkAvoidSweepThreshold
	 * parkAvoidSweepIncrement
	 * parkManeuverAngle
	 * parkManeuverDistance
	 * parkCenterDistance
	 * parkAngle
	 * parkBackInDistance
	 * parkDelay
	 * pulloutSweepStart
	 * pulloutSweepEnd
	 * pulloutSweepThreshold
	 * pulloutSweepIncrement
	 * sonarSweepTimeout
	 */
	public static final Map<String, Quantity> robotConstants = new TreeMap<>();
	/**
	 * Fields:
	 * leftMotor
	 * rightMotor
	 * frontMotor
	 * frontSensor
	 * leftSensor
	 * rightSensor
	 * sonicSensor
	 */
	public static final Map<String, Port> portMappings = new TreeMap<>();
	/**
	 * Fields:
	 * blendColors
	 */
	public static final Map<String, Set<String>> colorGroupings = new TreeMap<>();
	/**
	 * Fields:
	 * roadColor
	 * parkingColor
	 * intersectionColor
	 * fastColor
	 * slowColor
	 */
	public static final Map<String, String> colorFunctions = new TreeMap<>();
	/**
	 * Fields:
	 * minimumBlend
	 */
	public static final Map<String, Double> colorTolerances = new TreeMap<>();
	/**
	 * The color palette used for color matching.
	 */
	public static final ColorPalette allColors = new ColorPalette();
	/**
	 * The names of all the colors.
	 */
	public static final Set<String> colorNames = new TreeSet<>();
	{
		colorNames.add("red");
		colorNames.add("yellow");
		colorNames.add("white");
		colorNames.add("blue");
		colorNames.add("black");
	}

	// Using these maps is better than using an if-else chain; a map get is O(log(n)) while an if-else chain is O(n).
	/**
	 * The units supported when parsing strings.
	 */
	public static final Map<String, Unit> supportedUnits = new TreeMap<>();
	{
		supportedUnits.put("m/s", Unit.METER_PER_SECOND);
		supportedUnits.put("m", Unit.METER);
		supportedUnits.put("cm", Unit.CENTIMETER);
		supportedUnits.put("cm/s", Unit.CENTIMETER_PER_SECOND);
		supportedUnits.put("m/min", Unit.divide(Unit.METER, Unit.MINUTE));
		supportedUnits.put("m/s^2", Unit.METER_PER_SQUARE_SECOND);
		supportedUnits.put("s", Unit.SECOND);
		supportedUnits.put("mm", Unit.MILLIMETER);
		supportedUnits.put("deg", Unit.DEGREE);
		supportedUnits.put("deg/s", Unit.DEGREE_PER_SECOND);
		supportedUnits.put("", Unit.ONE);
	}

	/**
	 * The map that allows conversions from string to port objects.
	 */
	public static final Map<String, Port> portStrings = new TreeMap<>();
	{
		portStrings.put("A", MotorPort.A);
		portStrings.put("B", MotorPort.B);
		portStrings.put("C", MotorPort.C);
		portStrings.put("D", MotorPort.D);
		portStrings.put("1", SensorPort.S1);
		portStrings.put("2", SensorPort.S2);
		portStrings.put("3", SensorPort.S3);
		portStrings.put("4", SensorPort.S4);
	}

	// Side indications for parking, sensors or any other uses.
	/**
	 * Represents the left side of the robot.
	 */
	public static final int LEFT = 0;
	/**
	 * Represents the right side of the robot.
	 */
	public static final int RIGHT = 1;
	/**
	 * Represents the front of the robot.
	 */
	public static final int FRONT = 2;

	/**
	 * The number of reports printed.
	 */
	private static int numReports = 0;
	/**
	 * A map for the color sensors.
	 * Fields:
	 * frontSensor
	 * leftSensor
	 * rightSensor
	 */
	private final static Map<String, ColorSensor> colorSensorMap = new TreeMap<>();
	/**
	 * The sonar used for collision avoidance.
	 */
	private static Sonar sonar;

	/**
	 * Initializes all the maps with constants
	 */
	private static void initializeMaps() {

		try {

			// read all data
			Map<String, String> config = IO.readMap("ionconfig.txt");
			for (String key : config.keySet()) {
				String[] keySplit = key.split("\\.", 2);
				if (keySplit.length != 2)
					throw new IlegalFileFormatException("Key wrong:" + key);
				switch (keySplit[0]) {
				case "courseConstants":
					ION2016.courseConstants.put(keySplit[1], parseQuantity(config.get(key)));
					break;
				case "robotConstants":
					ION2016.robotConstants.put(keySplit[1], parseQuantity(config.get(key)));
					break;
				case "portMappings":
					ION2016.portMappings.put(keySplit[1], portStrings.get(config.get(key).toUpperCase().trim()));
					break;
				case "colorFunctions":
					ION2016.colorFunctions.put(keySplit[1], config.get(key));
					break;
				case "colorTolerances":
					ION2016.colorTolerances.put(keySplit[1], Double.parseDouble(config.get(key)));
					break;
				case "colorGroupings":
					if (ION2016.colorGroupings.get(keySplit[1]) == null)
						ION2016.colorGroupings.put(keySplit[1], new TreeSet<String>());
					ION2016.colorGroupings.get(keySplit[1]).add(config.get(key));
					break;
				}
			}

		} catch (FileNotFoundException e) {
			report("Reading map file failed");
			e.printStackTrace();
		}
	}

	/**
	 * Saves all the configuration data for the next run.
	 */
	public static void saveMaps() {

		// Build reverse unit maps
		Map<Unit, String> reverseUnits = new HashMap<>();
		for (String key : supportedUnits.keySet())
			reverseUnits.put(supportedUnits.get(key), key);

		Map<String, String> output = new TreeMap<>();
		output.putAll(quantityMapToStringMap(courseConstants, reverseUnits, "courseConstants"));
		output.putAll(quantityMapToStringMap(robotConstants, reverseUnits, "robotConstants"));
		for (String key : portMappings.keySet())
			output.put("portMappings." + key, portMappings.get(key).getName().replaceAll("S", ""));
		for (String key : colorFunctions.keySet())
			output.put("colorFunctions." + key, colorFunctions.get(key));
		for (String key : colorTolerances.keySet())
			output.put("colorTolerances." + key, colorTolerances.get(key) + "");
		for (String key : colorGroupings.keySet())
			for (String color : colorGroupings.get(key))
				output.put("colorGroupings." + key, color);

		try {
			IO.saveMap(output, "ionconfig.txt");
		} catch (FileNotFoundException e) {
			ION2016.report("Saving maps failed!");
			Button.waitForAnyPress();
		}
	}

	/**
	 * Converts a map of strings to quantities to a map of strings to string.
	 *
	 * @param map
	 *            the map to convert
	 * @param unitMap
	 *            the map which holds the unit to string conversions
	 * @param prefix
	 *            the prefix to add to the key in the result map
	 * @return the result map
	 */
	public static Map<String, String> quantityMapToStringMap(Map<String, Quantity> map, Map<Unit, String> unitMap, String prefix) {

		Map<String, String> output = new TreeMap<String, String>();
		for (String key : map.keySet()) {
			Quantity value = map.get(key);
			output.put(prefix + "." + key, value.getValueIn(value.getUnits()) + " " + unitMap.get(value.getUnits()));
		}
		return output;
	}

	/**
	 * Converts a string into a quantity with units.
	 *
	 * @param data
	 *            the data.
	 * @return the quantity representing the data.
	 */
	private static Quantity parseQuantity(String data) {

		String[] split = data.split(" ", 2);// Separate the data from the units
		Unit units;
		if (split.length == 2) {
			split[1] = split[1].trim().toLowerCase().replaceAll(" ", "");
			units = supportedUnits.get(split[1]);
		} else
			units = Unit.ONE;

		if (units == null)
			throw new IllegalArgumentException("Units: " + split[1] + " are unknown");
		return new Quantity(Double.parseDouble(split[0]), units);
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            does not use any arguments
	 */
	public static void main(String... args) {

		ExecutorService alabama = Executors.newFixedThreadPool(5);

		new Thread("Stopper") {

			@Override
			public void run() {

				while (true)
					if (Button.ESCAPE.isDown() && Button.ENTER.isDown()) {
						ION2016.saveMaps();
						ION2016.report("Maps saved");
						if (sonar != null)
							sonar.distanceAt(0);
						System.exit(0);
					}
			}
		}.start();

		// Give progress reports
		report("Started");
		new ION2016();

		// Initialize the data
		initializeMaps();
		report("Maps initialized");
	
		Future<ColorSensor> frontFuture = alabama.submit(new ColorSensorInitializer(portMappings.get("frontSensor"), true));
		Future<ColorSensor> leftFuture = alabama.submit(new ColorSensorInitializer(portMappings.get("leftSensor"),false));
		Future<ColorSensor> rightFuture = alabama.submit(new ColorSensorInitializer(portMappings.get("rightSensor"),false));
		alabama.submit(new Runnable() {

			@Override
			public void run() { 

				sonar = new Sonar(new EV3MediumRegulatedMotor(ION2016.portMappings.get("frontMotor")), ION2016.portMappings.get("sonicSensor"),
						robotConstants.get("sonarMaxAngle"), robotConstants.get("sonarGearRatio"));
				report("Made sonar");
			}
		});
		alabama.shutdown();

		try {
			alabama.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e1) {
			ION2016.report("alabama passed legislation banning death penalty");
			System.exit(-1);
		}

		try {
	
			colorSensorMap.put("frontSensor", frontFuture.get());
			colorSensorMap.put("leftSensor", leftFuture.get());
			colorSensorMap.put("rightSensor", rightFuture.get());
		} catch (InterruptedException | ExecutionException e1) {
			ION2016.report("Color sensor map initialization failed");
			e1.printStackTrace();
			System.exit(-1);
		}

		report("Sensor initialized");

		// Calibrate the black and white balances
		calibrateBalance();
		report("Calibrated balance");

		report("Waiting for button press");
		// Check to see if the enter button is held down. Wait at least 3000 ms (3 s). If not zero, a button was hit.
		if (0 != Button.waitForAnyPress(3000)) {
			report("Button pressed");
			report("Entering calibration");
			Map<String, Color> calData = calibrateColors();
			try {
				IO.saveMap(calData, "calibration.txt");
			} catch (FileNotFoundException e) {
				report("Saving calibration data failed");
				e.printStackTrace();
			}
			report("Calibrated");
		} else
			report("No button pressed");

		try {
			Map<String, String> calibration;
			calibration = IO.readMap("calibration.txt");
			for (String colorName : calibration.keySet())
				allColors.add(Color.parseColor(calibration.get(colorName)), colorName);
		} catch (FileNotFoundException e) {
			report("Reading calibration data failed");
			e.printStackTrace();
		}
		report("Calibration data loaded");

		// Make and start the robot controller

		// DifferentialPilot dp = new DifferentialPilot(ION.robotConstants.get("trackWidth"), ION.robotConstants.get("wheelDiameter"),
		// new EV3LargeRegulatedMotor(ION.portMappings.get("leftMotor")), new EV3LargeRegulatedMotor(ION.portMappings.get("rightMotor")));
		DifferentialPilot dp = new DifferentialPilot(ION2016.robotConstants.get("wheelDiameter").getValueIn(Unit.METER), ION2016.robotConstants.get(
				"trackWidth").getValueIn(Unit.METER), new EV3LargeRegulatedMotor(ION2016.portMappings.get("leftMotor")), new EV3LargeRegulatedMotor(
						ION2016.portMappings.get("rightMotor")));
		dp.setTravelSpeed(new Quantity(6, Unit.divide(Unit.METER, Unit.MINUTE)).getValueIn(Unit.METER_PER_SECOND));
		dp.setRotateSpeed(robotConstants.get("rotateSpeed").getValueIn(Unit.DEGREE_PER_SECOND));

		report("Made differential pilot");
		LineMonitor lineMonitor = new LineMonitor(colorSensorMap.get("leftSensor"), colorSensorMap.get("rightSensor"),
				colorSensorMap.get("frontSensor"), allColors, colorGroupings.get("blendColors"), colorFunctions.get("roadColor"));
		report("Made line monitor");
		try {
			alabama.awaitTermination(300, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		report("Calibrate sonar");
		sonar.calibrateSonar();
		report("Sonar calibrated");
		
		//XXX DUMB
		 //CourseController cc = new SimpleCourseController(new LineFollowAction("0", "1", 0), new IntersectionAction(-90, 1), new
		 //LineFollowAction("1",
		 //"2", 2));
		String fileName = "lotst.txt"; //CHANGE LAST LETTER OF FILENAME WHEN AT ION
		String cityMapName = "testmap.txt";//XXX change this for the comp 
		Set<Integer> lots = new TreeSet<>();
		int base = 1;// 1 is old base, 2 is new base
		try {
			Scanner s = new Scanner(new File(fileName));
			cityMapName = s.nextLine();
			base = Integer.parseInt(s.nextLine());
			MapCourseController.MAP_NAME = cityMapName;
			while (s.hasNextLine())
				lots.add(Integer.parseInt(s.nextLine()));
			s.close();
		} catch (FileNotFoundException e) {
			ION2016.report("Error getting lots from file.");
			e.printStackTrace();
			System.exit(-1);
		}

		
		ION2016.report("Starting base: " + base + "; 1 is old, 2 is new");
		ION2016.report("Parking spots: " + lots);
		// XXX
		// Smart
	
		CourseController cc = new MapCourseController(lots, base);///XXX IMPORTANT TO CHANGE FOR INF LINE FOLLOWING 
		//long endTime = System.currentTimeMillis();
		
		// Dumb!
		// CourseController cc = new SimpleCourseController(new LineFollowAction("0", "1", 0));
		//	report("Made course controller, time: " + (endTime - startTime) + " ms");
		RobotController controller = new RobotController(cc, lineMonitor, sonar, dp);
		report("Made robot controller");

		
		
		report("Ready. Press button to start.");
		Button.waitForAnyPress();
		// Starts the robot
		controller.start();
		report("RobotController started");
		// Waits for the end
		controller.waitForEnd();
		report("Done");

		System.exit(0);
	}

	/**
	 * Prints the specified format string with a debug level of 0. Prints out a message in the format [level:message number] string.
	 *
	 * @param str
	 *            the format string to print.
	 */
	public static synchronized void report(String str) {

		debug(0, str);
	}

	/**
	 * Prints the specified format spartring with a debug level of 0. Prints out a message in the format [level:message number] string.
	 *
	 * @param str
	 *            the format string to print.
	 * @param args
	 *            the format string arguments. See {@link String#format(String, Object...)}.
	 */
	public static synchronized void report(String str, Object... args) {

		debug(0, str, args);
	}

	/**
	 * Prints the specified format string if the debugLevel<={@link #DEBUG_LEVEL}. Prints out a message in the format [level:message number] string.
	 *
	 * @param debugLevel
	 *            the level.
	 * @param str
	 *            the format string to print.
	 * @param args
	 *            the format string arguments. See {@link String#format(String, Object...)}.
	 */
	public static synchronized void debug(int debugLevel, String str, Object... args) {

		System.out.println("[" + debugLevel + ":" + numReports + "] " + String.format(str, args));
		numReports++;
	}

	/**
	 * Prints the specified string if the debugLevel<={@link #DEBUG_LEVEL}. Prints out a message in the format [level:message number] string.
	 *
	 * @param debugLevel
	 *            the level.
	 * @param str
	 *            the format string to print.
	 */
	public static void debug(int debugLevel, String str) {

		debug(debugLevel, str, new Object[] {});
	}
	
	/**
	 * Calibrates the white and black balance for all the sensors
	 */
	private static void calibrateBalance() {

		report("Place all sensors over white");
		Button.waitForAnyPress();
		for (String key : colorSensorMap.keySet())
			colorSensorMap.get(key).calibrateWhite();
		report("Place all sensors over black");
		Button.waitForAnyPress();
		for (String key : colorSensorMap.keySet())
			colorSensorMap.get(key).calibrateBlack();
	}

	/**
	 * Calibrates the colors
	 *
	 * @return a map containing the calibration data. The map maps color names to color values.
	 */
	private static Map<String, Color> calibrateColors() {

		Map<String, Color> calibrationData = new TreeMap<>();
		for (String color : colorNames) {
			report("Place front sensor over " + color);
			Button.waitForAnyPress();
			if(calibrationData ==  null)
				report("calibrate map is null");
			calibrationData.put(color, colorSensorMap.get("frontSensor").getColor());
		}

		return calibrationData;
	}

	/**
	 * Interactively lets the user set a value.
	 *
	 * @param name
	 *            the name of the value to set
	 * @param current
	 *            the current value
	 * @return the value after the change
	 */
	public static double interactiveSet(String name, double current) {

		while (Button.ENTER.isUp()) {
			ION2016.report(String.format(name + "=%.8f", current));
			if(name.equalsIgnoreCase("p")){
				if (Button.UP.isDown())
					current += 15;
				else if (Button.DOWN.isDown())
					current -= 15;

			}

			else{
				if (Button.UP.isDown())
					current += .5;
				else if (Button.DOWN.isDown())
					current -= .5;
			}
		}
		ION2016.report(String.format(name + "set to %.8f", current));
		return current;
	}

}
