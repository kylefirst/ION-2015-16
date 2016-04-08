package main;

import static main.ION2016.FRONT;
import static main.ION2016.LEFT;
import static main.ION2016.RIGHT;

import java.util.Observable;
import java.util.Set;

import sensing.ColorSensor;
import utilities.Color;
import utilities.ColorPalette;

/**
 * @author jacob
 *
 */
public class LineMonitor extends Observable {

	/**
	 * The number of sensors
	 */
	private static final int NUM_SENSORS = 3;

	/**
	 * An array with all the sensors
	 */
	private final ColorSensor[] sensors;
	/**
	 * An array with all the latest sensor readings
	 */
	private final Color[] readings;
	/**
	 * An array with all the latest color matches
	 */
	private final String[] colorMatches;
	/**
	 * An array with all the latest color blends
	 */
	private final String[] colorBlends;
	/**
	 * An array with all the latest blend compositions
	 */
	private final double[] blendCompositions;
	/**
	 * An array with the sensor locks, used for ensuring the reading and writing of sensor data does not interleave
	 */
	private final Object[] sensorLocks;
	/**
	 * The color palette used for matching colors
	 */
	private final ColorPalette colors;
	/**
	 * The list of colors that might be found in a blend. Namely, white, yellow, and blue.
	 */
	private final Set<String> blendColors;
	/**
	 * The road color
	 */
	private final String roadColor;
	/**
	 * Flags which indicate if a specified sensor has triggered an event. For example, if the front sensor triggers an intersection event,
	 * flags[FRONT] is set to true until the sensor leaves the red line. This prevents multiple events for the same thing.
	 */
	private final boolean[] flags;
	/**
	 * The daemon that gathers sensor data.
	 */
	private final UtilityDaemon daemon;

	/**
	 * Makes a new LineMontior with all the specified information. This does not start the daemon thread; {@link #startDaemon()} must be called to do
	 * that.
	 *
	 * @param left
	 *            the color sensor on the left side of the robot.
	 * @param right
	 *            the color sensor on the right side of the robot.
	 * @param front
	 *            the color sensor in front of the robot.
	 * @param colors
	 *            all the colors on the course, to be used for color matching.
	 * @param blendColors
	 *            the colors that the road color might be blended with. Namely, white, yellow and blue.
	 * @param roadColor
	 *            the color of the road
	 */
	public LineMonitor(ColorSensor left, ColorSensor right, ColorSensor front, ColorPalette colors, Set<String> blendColors, String roadColor) {
	
		// Set up the array of sensors
		sensors = new ColorSensor[NUM_SENSORS];
		sensors[LEFT] = left;
		sensors[RIGHT] = right;
		sensors[FRONT] = front;

		// Set up the arrays of data
		readings = new Color[NUM_SENSORS];
		colorMatches = new String[NUM_SENSORS];
		colorBlends = new String[NUM_SENSORS];
		blendCompositions = new double[NUM_SENSORS];
		sensorLocks = new Object[NUM_SENSORS];
		for (int x = 0; x < sensorLocks.length; x++)
			sensorLocks[x] = new Object();
		this.colors = colors;
		this.blendColors = blendColors;
		this.roadColor = roadColor;
		flags = new boolean[NUM_SENSORS];

		// Flags stores the state of events. If the front sensor detects an intersection, then it should not keep sending detected messages until the
		// robot leaves the intersection. With the flags, as soon as this class sends one intersection detected message, it will set flags[FRONT] to
		// true, and will not send any more. Then, when the front sensor is not longer over an intersection, flags[FRONT] will be set to false. This
		// will be the same for the other sensors, though it will be parking and not intersections.
		for (int x = 0; x < flags.length; x++)
			flags[x] = false;

		// Set up and start the thread, which will, as isRunning is false, immediately go to sleep.
		daemon = new UtilityDaemon(new SensorDaemon());
	}

	/**
	 * Starts the sensor daemon thread and starts getting data.
	 */
	public void startDaemon() {
	
		daemon.startDaemon();
	}

	/**
	 * Pauses the daemon thread. The thread can be restarted with {@link #startDaemon()}.
	 */
	public void pauseDeamon() {
	
		daemon.pauseDeamon();
	}

	/**
	 * Terminates the daemon thread. This is irreversible; the thread cannot be restarted. Compare to {@link #pauseDeamon()}.
	 */
	public void terminateDeamon() {
	
		daemon.terminateDeamon();
	}

	/**
	 * Gets the latest reading from the sensor with the specified ID.
	 *
	 * @param ID
	 *            the id of the sensor. Should be one of the following: {@link ION2016#LEFT}, {@link ION2016#RIGHT}, {@link ION2016#FRONT}.
	 * @return the most recent reading from the specified sensor. Should never return null.
	 */
	public Color getColor(int ID) {
	
		if (!daemon.isRunning())
			throw new IllegalStateException("This daemon is not running (terminated or paused).");

		synchronized (sensorLocks[ID]) {
			return readings[ID];
		}
	}

	/**
	 * Gets the latest color match from the sensor.
	 *
	 * @param ID
	 *            the id of the sensor. Should be one of the following: {@link ION2016#LEFT}, {@link ION2016#RIGHT}, {@link ION2016#FRONT}.
	 * @return the most recent color match. Can be null if there is no good match.
	 */
	public String getColorMatch(int ID) {
	
		if (!daemon.isRunning())
			throw new IllegalStateException("This daemon is not running (terminated or paused).");

		synchronized (sensorLocks[ID]) {
			return colorMatches[ID];
		}
	}

	/**
	 * Gets the latest color blend from the specified sensor.
	 *
	 * @param ID
	 *            the id of the sensor. Should be one of the following: {@link ION2016#LEFT}, {@link ION2016#RIGHT}, {@link ION2016#FRONT}.
	 * @return the color that is blended with the road color to make the sensor reading. This should never be null; there should always be a best
	 *         blend.
	 */
	public String getBlend(int ID) {
	
		if (!daemon.isRunning())
			throw new IllegalStateException("This daemon is not running (terminated or paused).");

		synchronized (sensorLocks[ID]) {
			return colorBlends[ID];
		}
	}

	/**
	 * Gets the latest blend composition from the specified sensor.
	 *
	 * @param ID
	 *            the id of the sensor. Should be one of the following: {@link ION2016#LEFT}, {@link ION2016#RIGHT}, {@link ION2016#FRONT}.
	 * @return the composition of the blend. This method assumes that the sensor reading is a blend of two colors: the road color, and the color
	 *         returned by {@link #getBlend(int)}. Then, this method uses {@link Color#determineComposition(Color, Color)} to determine the blend. If
	 *         the sensor reading is equal to the color returned by {@link #getBlend(int)}, then this method will return a value close to 1. If the
	 *         sensor reading is equal to the road color, then this method will return a value close to 0.
	 */
	public double getBlendComposition(int ID) {
	
		if (!daemon.isRunning())
			throw new IllegalStateException("This daemon is not running (terminated or paused).");

		synchronized (sensorLocks[ID]) {
			return blendCompositions[ID];
		}
	}

	/**
	 * Gets the blend data by returning an object array with the string color and then the number.
	 *
	 * @param ID
	 *            the ID of the sensor to get blend data for.
	 * @return an object array with he string color and then the number.
	 */
	public Object[] getAllBlendData(int ID) {
	
		if (!daemon.isRunning())
			throw new IllegalStateException("This daemon is not running (terminated or paused).");

		synchronized (sensorLocks[ID]) {
			return new Object[] { colorBlends[ID], blendCompositions[ID] };
		}
	}

	/**
	 * Reads and stores all the sensor data. This method also notifies any observers.
	 */
	private void readSensorData() {

		// Read the data from all the sensors
		for (int x = 0; x < sensorLocks.length; x++)
			synchronized (sensorLocks[x]) {
				readings[x] = sensors[x].getColor();
				colorMatches[x] = colors.getBestMatch(readings[x]);
				colorBlends[x] = colors.getBestBlend(blendColors, roadColor, readings[x]);
				blendCompositions[x] = readings[x].determineComposition(colors.getColor(colorBlends[x]), colors.getColor(roadColor));
			}

		//ION2016.report("RED: " + readings[ION2016.FRONT].getRed());
		if (readings[ION2016.FRONT].getRed() > .4) { //old value is .15
			setChanged();
			notifyObservers(RobotMessage.INTERSECTION_DETECTED);
			flags[FRONT] = true;

		} else if (readings[ION2016.FRONT].getRed() <= .4)
			flags[FRONT] = false;

		// TODO Try this!!
		// Color testColor = readings[ION.FRONT];
		// System.out.printf("%.2f,%.2f,%.2f", testColor.getRed(), testColor.getGreen(), testColor.getBlue());
		// if ("red".equals(colorBlends[ION.FRONT])) {
		// // System.out.println("Ah!!!");
		// // Button.waitForAnyPress();
		// }
		//
		// System.out.println(colorBlends[ION.FRONT]);

		// Intersection detection
		// // If the flag is not set and the front color is red, send an intersection detected message and set the flag
		// if (!flags[FRONT] && colorMatches[FRONT].equals(ION.colorFunctions.get("intersectionColor"))) {
		// // TODO Fix stop sign identification
		// setChanged();
		// notifyObservers(RobotMessage.INTERSECTION_DETECTED);
		// flags[FRONT] = true;
		// }
		// // If the color is not red, reset the flag
		// if (colorMatches[FRONT].equals(ION.colorFunctions.get("intersectionColor")))
		// flags[FRONT] = false;

		// Parking lot detection
		if (parkingCheck(LEFT)) {
			setChanged();
			notifyObservers(RobotMessage.PARKING_LOT_LEFT_DETECTED);
		}
		if (parkingCheck(RIGHT)) {
			setChanged();
			notifyObservers(RobotMessage.PARKING_LOT_RIGHT_DETECTED);
		}
	}

	/**
	 * Checks to see if a parking lot detected message needs to be sent out.
	 *
	 * @param side
	 *            the side on which to check
	 * @return true if a parking lot detected message should be sent out, false otherwise
	 */
	private boolean parkingCheck(int side) {
	
		String parkingColor = ION2016.colorFunctions.get("parkingColor");
		// If the flag is not set and the left color or color blend indicates blue, send an parking lot detected message and set the flag
		if (!flags[side])
			// A parking lot will be detected if either the color match is blue, or the blend is blue, with a composition above a certain
			// threshold (when mostly black blends are detected, the certainty of the other color is low).
			if (colorMatches[side].equals(parkingColor) || colorBlends[side].equals(parkingColor)
					&& blendCompositions[side] > ION2016.colorTolerances.get("minimumBlend")) {
				flags[side] = true;
				return true;
			}
		// If the color is not red, reset the flag
		if (!(colorMatches[side].equals(parkingColor) || colorBlends[side].equals(parkingColor)
				&& blendCompositions[side] > ION2016.colorTolerances.get("minimumBlend")))
			flags[side] = false;

		return false;
	}

	/**
	 * The sensor daemon class that gathers data from all the sensors.
	 *
	 * @author jacob
	 *
	 */
	private class SensorDaemon implements Runnable {

		/**
		 * Runs the sensor daemon.
		 */
		@Override
		public void run() {
		
			readSensorData();
		}
	}
}