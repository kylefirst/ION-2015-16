package main;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import lejos.hardware.Sound;
import utilities.Color;
import utilities.Delay;
import utilities.units.Quantity;
import utilities.units.Unit;

/**
 * This class controls the robot. It receives events from all observable things, such as the LineMontior, and then uses those events to control the
 * robot.
 *
 * @author jacob
 *
 */
public class RobotController implements Observer {
	
	/**
	 * The course controller used to determine the course.
	 */
	private final CourseController courseController;
	/**
	 * The differential pilot used to drive the robot.
	 */
	private final DifferentialPilot pilot;
	/**
	 * The line monitor used to read the sensors.
	 */
	private final LineMonitor lineMonitor;
	/**
	 * The sonar used to detect traffic
	 */
	private final Sonar sonar;
	/**
	 * The line follower.
	 */
	private final LineFollower lf;
	/**
	 * The executor service used to run all the actions.
	 */
	private final ExecutorService actionExecutor;
	/**
	 * The status of the currently running task.
	 */
	private Future<?> taskStatus;
	/**
	 * True if the robot has completed the course, false otherwise.
	 */
	private final AtomicBoolean isDone;
	
	/**
	 * Makes a new robot controller that will control the robot to visit the specified lots.
	 *
	 * @param c
	 *            the course controller to use.
	 * @param lm
	 *            the line monitor to use.
	 * @param s
	 *            the sonar to use.
	 * @param dp
	 *            the differential pilot to use.
	 *
	 */
	public RobotController(CourseController c, LineMonitor lm, Sonar s, DifferentialPilot dp) {
	
		// Set up everything
		courseController = c;
		lineMonitor = lm;
		pilot = dp;
		sonar = s;
		
		actionExecutor = Executors.newSingleThreadExecutor();// One thread forces sequential operation of all tasks
		
		isDone = new AtomicBoolean(false);
		
		// Set up the observing
		lineMonitor.addObserver(this);
		sonar.addObserver(this);
		sonar.setAlertThreshold(ION2016.robotConstants.get("sonarAlertThreshold").getValueIn(Unit.METER));
		
		// Set up line following
		lf = new LineFollower(ION2016.robotConstants.get("lineFollowingP").getValueIn(Unit.ONE), ION2016.robotConstants.get("lineFollowingI").getValueIn(
				Unit.ONE), ION2016.robotConstants.get("lineFollowingD").getValueIn(Unit.ONE), dp, lm);
	}
	
	/**
	 * Starts the robot controller by getting the initial task from the course controller.
	 */
	public void start() {
	
		// Start all daemons
		lineMonitor.startDaemon();
		sonar.startDaemon();
		
		// Get and run the initial task from the courseController
		RobotAction nextAction = courseController.getNextAction();
		taskStatus = actionExecutor.submit(new RobotTask(nextAction));
	}
	
	/**
	 * Waits for the robot to finish the course. Does not return until the robot finishes.
	 */
	public void waitForEnd() {
	
		synchronized (isDone) {
			while (!isDone.get())
				try {
					isDone.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * Receives updates from observable things. arg should always be a {@link RobotMessage}.
	 */
	@Override
	public void update(Observable o, Object arg) {
	
		// XXX
		// if (System.currentTimeMillis() > 0)
		// return;
		//courseController.logEvent((RobotMessage)arg);
		// ensure that everything is ignored while the executor is running
		if (taskStatus != null && taskStatus.isDone()) {
			if (!(arg instanceof RobotMessage))
				throw new IllegalArgumentException("arg is not a RobotMessage");
			
			RobotMessage message = (RobotMessage) arg;
			ION2016.report(message.toString());
			
			if (message == RobotMessage.APPROACHING_OBJECT) {
				// TODO improve rear end prevention.
				double speedCorrection = 1;
				double distance = RobotMessage.APPROACHING_OBJECT.getParameter();
				if (distance <= .05)
					speedCorrection = 0;
				else if (distance > .05 && distance <= .2)
					speedCorrection = (distance - .05) / (.2 - .05);
				
				double objectSpeed = ION2016.courseConstants.get("yellowSpeed").getValueIn(Unit.METER_PER_SECOND) * speedCorrection;
				
				ION2016.report("Object Speed %.2f: ", objectSpeed);
				lf.setMaxSpeed(objectSpeed);
				return;
			} else if (message == RobotMessage.ALL_CLEAR) {
				lf.setMaxSpeed(Double.POSITIVE_INFINITY);
				//pilot.setTravelSpeed(ION2016.courseConstants.get("yellowSpeed").getValueIn(Unit.METER_PER_SECOND));
				return;
			}
			
			//fix?
			// Inform the course controller
			courseController.logEvent(message);
			
			RobotAction nextAction = courseController.getNextAction();
			ION2016.report(nextAction.dataToString());
			
			if (!(lf.isLineFollowing() && nextAction instanceof LineFollowAction)) {
				
				stopLineFollowing();
				pilot.stop();
				if (message == RobotMessage.INTERSECTION_DETECTED) {
					Sound.beep();
					actionExecutor.submit(new RobotDelay(ION2016.courseConstants.get("stopDelay")));
				} else if (message == RobotMessage.PARKING_LOT_RIGHT_DETECTED)
					// We have to pause when entering a parking lot
					actionExecutor.submit(new RobotDelay(ION2016.robotConstants.get("parkDelay")));
				
				taskStatus = actionExecutor.submit(new RobotTask(nextAction));
			}
		}
	}
	
	/**
	 * Stops line following, but does not stop the robot.
	 */
	public void stopLineFollowing() {
	
		ION2016.report("Stopped Line Following");
		lf.stopLineFollowing();
		sonar.pauseDeamon();
	}
	
	/**
	 * Returns true if the robot is line following now, false otherwise.
	 *
	 * @return true if the robot is line following now, false otherwise.
	 */
	public boolean isLineFollowing() {
	
		return lf.isLineFollowing();
	}
	
	/**
	 * Makes the robot execute a line following action. This method starts line following and returns once line following has started. Also, line
	 * following should take care of speed limits.
	 *
	 * @param lfa
	 *            the line following action to execute.
	 */
	public void executeAction(LineFollowAction lfa) {
	
		ION2016.report("Starting line following");
		lf.setMaxSpeed(Double.POSITIVE_INFINITY);
		lf.startLineFollowing();
		sonar.startDaemon();
	}
	
	/**
	 * Makes the robot go through the intersection in the manner indicated by the action. When the robot gets through the intersection, this action
	 * should log a intersection completed event with the course controller. If the course controller then asks the robot to continue line following,
	 * this action should submit the line follow task to the executor before exiting.
	 *
	 * @param ia
	 *            the intersection action to execute.
	 */
	public void executeAction(IntersectionAction ia) {
	
		// Get through the intersection in the manner indicated by the action. When the robot gets through the intersection, this action should
		// log a intersection completed event with the course controller. If the course controller then asks the robot to continue line following,
		// this action should submit the line follow task to the executor before exiting.
		// RobotMessage.INTERSECTION_NAVIGATED
		ION2016.report("Started Intesection Action");
		
		sonarSweepTimeout((int) ION2016.robotConstants.get("intersectionSweepStart").getValueIn(Unit.DEGREE),
				(int) ION2016.robotConstants.get("intersectionSweepEnd").getValueIn(Unit.DEGREE), ION2016.robotConstants.get("intersectionSweepThreshold")
						.getValueIn(Unit.METER), (int) ION2016.robotConstants.get("intersectionSweepIncrement").getValueIn(Unit.DEGREE),
				(long) ION2016.robotConstants.get("sonarSweepTimeout").getValueIn(Unit.MILLISECOND));
		
		Object[] blendDataLeft = lineMonitor.getAllBlendData(ION2016.LEFT);
		Object[] blendDataRight = lineMonitor.getAllBlendData(ION2016.RIGHT);
		
		double errorLeft = 2 * ((double) blendDataLeft[1] - .5);
		double errorRight = 2 * ((double) blendDataRight[1] - .5);
		double posError = LineFollower.error(errorLeft, errorRight, .5);
		ION2016.report("Starting intersection, pos error=%.2f", posError);
		// + -> right, - -> left
		int angle = ia.getAngle();
		
		// Pull up so that the center of the robot is over the red line
		pilot.travel(ION2016.robotConstants.get("intersectionPullup").getValueIn(Unit.METER));
		pilot.arc(-1 * Math.signum(angle) * ION2016.courseConstants.get("roadWidth").getValueIn(Unit.METER) / 2,
				-1 * Math.signum(angle) * Math.abs(angle));
		
		courseController.logEvent(RobotMessage.INTERSECTION_NAVIGATED);
		
		/*RobotAction next = courseController.getNextAction();
		
		if (next instanceof PulloutAction){
			PulloutAction side = (PulloutAction)next;
			int whichSideBro = side.getSide();
			
			if(whichSideBro == ION2016.RIGHT){
				
				//scan left 
			boolean objLeft = sonar.sweep(-90, 0, .3 , 5);
			
			while(objLeft){
				
				pilot.travel(2);
				objLeft = sonar.sweep(-90, 90, .3 , 5);
				Delay.delay(3000);
			}
				
			}
		}
		*/
		
		ION2016.report("About to go to next intersection");
		
		RobotAction nextAction = courseController.getNextAction();
		ION2016.report(nextAction.toString());
		taskStatus = actionExecutor.submit(new RobotTask(nextAction));
		
	}
	
	/**
	 * Aligns with line using dank memes
	 */
	private void alignWithRedLine() {
	
		String intersectionColor = ION2016.colorFunctions.get("intersectionColor");
		String roadColor = ION2016.colorFunctions.get("roadColor");
		double initialBlend = lineMonitor.getColor(ION2016.FRONT).determineComposition(ION2016.allColors.getColor(intersectionColor),
				ION2016.allColors.getColor(roadColor));
		if (Math.abs(initialBlend - .5) > .3) {
			pilot.setTravelSpeed(ION2016.robotConstants.get("parkingSpeed").getValueIn(Unit.METER_PER_SECOND));
			if (initialBlend > .9)
				pilot.forward();
			else
				pilot.backward();
			double blend = initialBlend;
			while (Math.abs(blend - .5) > .3)
				blend = lineMonitor.getColor(ION2016.FRONT).determineComposition(ION2016.allColors.getColor(intersectionColor),
						ION2016.allColors.getColor(roadColor));
			// dank XXX memes
			pilot.stop();
		}
		pilot.setTravelSpeed(ION2016.courseConstants.get("yellowSpeed").getValueIn(Unit.METER_PER_SECOND));
	}
	
	/**
	 * Makes the robot park in a parking space, and return once the robot is in the parking space. Do not exit the space. When parking is complete and
	 * this action should log a parking completed event with the course controller. If the course controller then asks the robot to pull out of the
	 * space, this action should submit a delay to the executor and then a pull out task to the executor before exiting.
	 *
	 * @param pa
	 *            the parking action to execute.
	 */
	public void executeAction(ParkAction pa) {
	
		// Get into a parking space, and return once the robot is in the parking space. Do not exit the space. When parking is complete and after
		// the robot has waited in the spot, this action should log a parking completed event with the course controller. If the course controller
		// then asks the robot to pull out of the space, this action should submit the pull out task to the executor before exiting.
		// RobotMessage.PARKED
		
		pilot.setTravelSpeed(ION2016.robotConstants.get("parkingSpeed").getValueIn(Unit.METER_PER_SECOND));
		int side = pa.getSide() == ION2016.RIGHT ? 1 : -1;
		
		// System.out.println(sonar.sweep(side * 90, side * 60, .3, 10));
		// Button.waitForAnyPress();
		int parkingSweepStart = (int) ION2016.robotConstants.get("parkingSweepStart").getValueIn(Unit.DEGREE);
		int parkingSweepEnd = (int) ION2016.robotConstants.get("parkingSweepEnd").getValueIn(Unit.DEGREE);
		double parkingSweepThreshold = ION2016.robotConstants.get("parkingSweepThreshold").getValueIn(Unit.METER);
		int parkingSweepIncrement = (int) ION2016.robotConstants.get("parkingSweepIncrement").getValueIn(Unit.DEGREE);
		long timeout = (long) ION2016.robotConstants.get("sonarSweepTimeout").getValueIn(Unit.MILLISECOND);
		
		if (!sonar.sweep(side * parkingSweepStart, side * parkingSweepEnd, parkingSweepThreshold, parkingSweepIncrement)) {
			
			// Back up and make sound
			double avoidDistance = ION2016.robotConstants.get("parkAvoidBackup").getValueIn(Unit.METER);
			pilot.travel(-avoidDistance, true);
			while (pilot.isMoving())
				Sound.beep();
			
			int parkAvoidSweepStart = (int) ION2016.robotConstants.get("parkAvoidSweepStart").getValueIn(Unit.DEGREE);
			int parkAvoidSweepEnd = (int) ION2016.robotConstants.get("parkAvoidSweepEnd").getValueIn(Unit.DEGREE);
			double parkAvoidSweepThreshold = ION2016.robotConstants.get("parkAvoidSweepThreshold").getValueIn(Unit.METER);
			int parkAvoidSweepIncrement = (int) ION2016.robotConstants.get("parkAvoidSweepIncrement").getValueIn(Unit.DEGREE);
			sonarSweepTimeout(side * parkAvoidSweepStart, side * parkAvoidSweepEnd, parkAvoidSweepThreshold, parkAvoidSweepIncrement, timeout);
			pilot.travel(avoidDistance);
		}
		
		double parkManeuverAngle = ION2016.robotConstants.get("parkManeuverAngle").getValueIn(Unit.DEGREE);
		double parkManeuverDistance = ION2016.robotConstants.get("parkManeuverDistance").getValueIn(Unit.METER);
		pilot.rotate(-parkManeuverAngle * (pa.getSide() == ION2016.LEFT ? -1 : 1));
		pilot.travel(parkManeuverDistance);
		pilot.rotate(parkManeuverAngle * (pa.getSide() == ION2016.LEFT ? -1 : 1));
		pilot.forward();
		waitForEdgeCrossings(1, pa.getSide());
		pilot.stop();
		
		pilot.travel(-ION2016.robotConstants.get("parkCenterDistance").getValueIn(Unit.METER));
		
		int angle = (int) ION2016.robotConstants.get("parkAngle").getValueIn(Unit.DEGREE) * (pa.getSide() == ION2016.LEFT ? -1 : 1);// -90 for left (turn
		// right to back in), 90
		// for right (turn left to
		// back in)
		pilot.rotate(angle);
		pilot.backward();
		// Button.waitForAnyPress();
		waitForEdgeCrossings(1, ION2016.LEFT, ION2016.RIGHT);
		pilot.travel(-ION2016.robotConstants.get("parkBackInDistance").getValueIn(Unit.METER));
		
		while (pilot.isMoving())
			Sound.beep();
		
		pilot.stop();
		Delay.delay(ION2016.robotConstants.get("parkDelay"));
		
		courseController.logEvent(RobotMessage.PARKED);
		RobotAction nextAction = courseController.getNextAction();
		taskStatus = actionExecutor.submit(new RobotTask(nextAction));
	}
	
	/**
	 * Makes the robot pull out of a parking space and turn so that the robot is facing in the proper direction to continue down the road. Must work
	 * on the starting space too. When the robot exits the space, this action should log a pull out completed event with the course controller. If the
	 * course controller 
	 * then asks the robot to continue line following, this action should submit the line follow task to the executor before
	 * exiting.
	 *
	 * @param pa
	 *            the pull out action to execute.
	 */
	public void executeAction(PulloutAction pa) {
	
		// Get out of the parking space and turn so that the robot is facing in the proper direction to continue down the road. Must work on the
		// starting space too. When the robot exits the space, this action should log a pull out completed event with the course controller. If the
		// course controller then asks the robot to continue line following, this action should submit the line follow task to the executor before
		// exiting.
		// RobotMessage.PULLED_OUT
		
		int pulloutSweepStart = (int) ION2016.robotConstants.get("pulloutSweepStart").getValueIn(Unit.DEGREE);
		int pulloutSweepEnd = (int) ION2016.robotConstants.get("pulloutSweepEnd").getValueIn(Unit.DEGREE);
		double pulloutSweepThreshold = ION2016.robotConstants.get("pulloutSweepThreshold").getValueIn(Unit.METER);
		int pulloutSweepIncrement = (int) ION2016.robotConstants.get("pulloutSweepIncrement").getValueIn(Unit.DEGREE);
		long timeout = (long) ION2016.robotConstants.get("sonarSweepTimeout").getValueIn(Unit.MILLISECOND);
		sonarSweepTimeout(pulloutSweepStart, pulloutSweepEnd, pulloutSweepThreshold, pulloutSweepIncrement, timeout);
		
		pilot.setTravelSpeed(ION2016.robotConstants.get("parkingSpeed").getValueIn(Unit.METER_PER_SECOND));
		pilot.travel(.05);
		pilot.forward();
		waitForEdgeCrossings(1, ION2016.FRONT);
		//pilot.travel(-.015);
		pilot.stop();
		int angle = 90 * (pa.getSide() == ION2016.LEFT ? 1 : -1);// 90 for left, -90 for right
		ION2016.report("Angle to turn: "+ (angle-10));
		pilot.rotate((angle-10));//XXX Dank Memes : 10 is for anglw correction
		
		courseController.logEvent(RobotMessage.PULLED_OUT);
		RobotAction nextAction = courseController.getNextAction();
		taskStatus = actionExecutor.submit(new RobotTask(nextAction));
	}
	
	/**
	 * Waits for the specified number of line crossings to occur. Returns as soon as the robot's specified sensor has crossed the specified number of
	 * lines.
	 *
	 * @param numLineCrossings
	 *            the number of lines the robot should cross.
	 * @param sensor
	 *            the sensor ID of the sensor that should detect the edges. Should be either {@link ION2016#FRONT}, {@link ION2016#LEFT}, or {@link ION2016#RIGHT}
	 */
	private void waitForLineCrossings(int numLineCrossings, int sensor) {
	
		waitForEdgeCrossings(numLineCrossings * 2, sensor);
	}
	
	/**
	 * Waits for the specified number of edge crossings to occur. Returns as soon as the robot's specified sensor has crossed the specified number of
	 * edges.
	 *
	 * @param numEdges
	 *            the number of edges the robot should cross.
	 * @param sensor
	 *            the sensor ID of the sensor that should detect the edges. Should be either {@link ION2016#FRONT}, {@link ION2016#LEFT}, or {@link ION2016#RIGHT}
	 *            .
	 * @return the sensor that crossed the edge
	 */
	private int waitForEdgeCrossings(int numEdges, int... sensor) {
	
		int[] numEdgesCrossed = new int[sensor.length];
		for (int x = 0; x < numEdgesCrossed.length; x++)
			numEdgesCrossed[x] = 0;
		
		String roadColor = ION2016.colorFunctions.get("roadColor");
		boolean[] overLine = new boolean[sensor.length];
		for (int x = 0; x < sensor.length; x++)
			overLine[x] = !lineMonitor.getColorMatch(sensor[x]).equals(roadColor);
		
		int winningSensor = -1;
		// numEdgesCrossed < numEdges
		while (winningSensor == -1)
			for (int x = 0; x < sensor.length; x++) {
				if (!overLine[x]) {
					if (!lineMonitor.getColorMatch(sensor[x]).equals(roadColor)) {
						overLine[x] = true;
						numEdgesCrossed[x]++;
					}
				} else if (overLine[x])
					if (lineMonitor.getColorMatch(sensor[x]).equals(roadColor)) {
						overLine[x] = false;
						numEdgesCrossed[x]++;
					}
				if (numEdgesCrossed[x] >= numEdges)
					winningSensor = x;
			}
		
		return winningSensor;
	}
	
	/**
	 * Will sweep from an angle to another angle and will have a timeout
	 *
	 * @param startAngle
	 *            the start angle, in degrees, from the equilibrium position. Positive is right, negative is left.
	 * @param endAngle
	 *            the end angle, in degrees, from the equilibrium position. Positive is right, negative is left.
	 * @param threshold
	 *            the distance required to return false.
	 * @param increment
	 *            the increment in degrees. Should be positive.
	 * @param timeout
	 *            timeout in millis
	 * @return Returns true if everything in the region is greater than threshold away.
	 */
	private boolean sonarSweepTimeout(int startAngle, int endAngle, double threshold, int increment, long timeout) {
	
		long startTime = System.currentTimeMillis();
		while (!sonar.sweep(startAngle, endAngle, threshold, increment))
			if (System.currentTimeMillis() - startTime > timeout)
				return true;
		return false;
	}
	
	/**
	 * This action is executed when the robot finishes. It should play a happy song, set isDone to true and notify, and return.
	 *
	 * @param ca
	 *            the celebrate action.
	 */
	public void executeAction(CelebrateAction ca) {
	
		// Play a happy song, set isDone to true and notify, and return.
		// YAY!!!!
		// TODO
		// TODO
		// XXX NEED TO ADD VOLUNTEER>WAV
		
		pilot.stop();// Ensure that we are stopped
		for (int x = 0; x < 5; x++)
			Sound.playSample(new File("volunteer.wav"), 100);
		synchronized (isDone) {
			isDone.set(true);
			isDone.notifyAll();
		}
	}
	
	/**
	 * Will wiggle in place until all sensors are on correct lines
	 */
	public double wiggle() {
	
		pilot.setTravelSpeed(ION2016.robotConstants.get("parkingSpeed").getValueIn(Unit.METER_PER_SECOND));
		Color intersectionColor = ION2016.allColors.getColor(ION2016.colorFunctions.get("intersectionColor"));
		Color roadColor = ION2016.allColors.getColor(ION2016.colorFunctions.get("roadColor"));
		
		// TODO add to config file
		int valueToRotate = 5;
		
		// 2 * (x - .5) makes a range from -1 to 1, -1 is the road and 1 is the line
		double error0 = 2 * (lineMonitor.getColor(ION2016.FRONT).determineComposition(intersectionColor, roadColor) - .5);
		pilot.rotate(valueToRotate);
		double errorLeft = 2 * (lineMonitor.getColor(ION2016.FRONT).determineComposition(intersectionColor, roadColor) - .5);
		pilot.rotate(-2 * valueToRotate);
		double errorRight = 2 * (lineMonitor.getColor(ION2016.FRONT).determineComposition(intersectionColor, roadColor) - .5);
		
		double dE = ((errorLeft - error0) / valueToRotate + (error0 - errorRight) / valueToRotate) / 2;
		double k = .2;// Constant that depends on the radius of the sensor readings
		double angle = Math.toDegrees(Math.asin(dE / k));
		ION2016.report("Wiggle dE: %.2f", dE);
		ION2016.report("Wiggle angle: %.2f", angle);
		pilot.rotate(valueToRotate - angle);
		
		Sound.twoBeeps();
		
		pilot.setTravelSpeed(ION2016.courseConstants.get("yellowSpeed").getValueIn(Unit.METER_PER_SECOND));
		return angle;
	}
	
	public double getBestAngle(double ratio, double valueToRotate, double min, double max, List<double[]> data) {
	
		double angleMin = Double.NaN;
		double angleMax = Double.NaN;
		
		for (double[] dataPoint : data)
			// On right side (min angle)
			if (dataPoint[0] < valueToRotate) {
				if (dataPoint[1] > min + (max - min) * ratio)
					angleMin = dataPoint[0];
			} else if (dataPoint[1] > min + (max - min) * ratio)
				angleMax = dataPoint[0];
		
		// If we found a min and max for the left sensor
		if (!Double.isNaN(angleMin) && !Double.isNaN(angleMax))
			return (angleMin + angleMax) / 2;
		else
			return Double.NaN;
	}
	
	protected double standardDeviation(double... stuff) {
	
		double mean = average(stuff);
		double stddev = 0;
		for (double element : stuff)
			stddev += Math.pow(element - mean, 2);
		return Math.sqrt(stddev / stuff.length);
	}
	
	private double average(double... stuff) {
	
		double total = 0;
		
		for (double element : stuff) {
			if (Double.isNaN(element))
				return Double.NaN;
			total += element; // this is the calculation for summing up all the values
		}
		
		return total / stuff.length;
	}
	
	/**
	 * Required to make the program compile. This method should never be called; only the above methods should actually be used.
	 *
	 * @param ra
	 *            the robot action.
	 */
	public void executeAction(RobotAction ra) {
	
		throw new IllegalArgumentException("Unable to execute " + ra.getClass().getSimpleName());
	}
	
	/**
	 * A class to make executing one of the above methods simple.
	 *
	 * @author jacob
	 *
	 */
	private class RobotTask implements Runnable {
		
		/**
		 * The robot action to run.
		 */
		private final RobotAction r;
		
		/**
		 * Makes a new robot task that executes the specified robot action.
		 *
		 * @param r
		 *            the robot action to run.
		 */
		public RobotTask(RobotAction r) {
		
			this.r = r;
		}
		
		/**
		 * Runs the robot action
		 */
		@Override
		public void run() {
		
			ION2016.report("Robot action run called " + r.toString());
			if (r instanceof LineFollowAction)
				executeAction((LineFollowAction) r);
			else if (r instanceof IntersectionAction)
				executeAction((IntersectionAction) r);
			else if (r instanceof ParkAction)
				executeAction((ParkAction) r);
			else if (r instanceof PulloutAction)
				executeAction((PulloutAction) r);
			else if (r instanceof CelebrateAction)
				executeAction((CelebrateAction) r);
		}
	}
	
	/**
	 * A class to make stopping the robot simple
	 *
	 * @author jacob
	 *
	 */
	private class RobotDelay implements Runnable {
		
		/**
		 * The amount of time to delay for
		 */
		private final Quantity delay;
		
		/**
		 * Makes a new delay task with the specified delay
		 *
		 * @param delay
		 *            the delay to use
		 */
		public RobotDelay(Quantity delay) {
		
			this.delay = delay;
		}
		
		@Override
		public void run() {
		
			Delay.delay(delay);
		}
	}
}
