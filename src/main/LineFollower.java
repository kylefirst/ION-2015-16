package main;

import lejos.hardware.Button;
import utilities.regulation.DerivativeController;
import utilities.units.Quantity;
import utilities.units.Unit;

/**
 * @author jacob
 *
 */
public class LineFollower {
	
	/**
	 * The proportional constant
	 */
	private volatile double p;
	/**
	 * The integral constant
	 */
	private volatile double i;
	/**
	 * The derivative constant
	 */
	private volatile double d;
	/**
	 * The pilot used to control the robot
	 */
	private final DifferentialPilot pilot;
	/**
	 * The line monitor that provides sensor data
	 */
	private final LineMonitor lineMonitor;
	/**
	 * The daemon which does the line following.
	 */
	private final UtilityDaemon lineFollower;
	/**
	 * Keeps time derivative of l-r
	 */
	private final DerivativeController angularCalculator;
	/**
	 * Keeps the derivative of angular error
	 */
	private final DerivativeController dAECalculator;
	/**
	 * The white speed, stored for speed. In m/s
	 */
	private final double whiteSpeed;
	/**
	 * The yellow speed, stored for speed. In m/s
	 */
	private final double yellowSpeed;
	/**
	 * The maximum speed of the line follower in m/s.
	 */
	private double maxSpeed = Double.POSITIVE_INFINITY;
	private final Object glueLock = new Object();
	private volatile boolean glueLeft = false;
	private volatile boolean glueRight = false;
	private final double glueValue = 0;
	
	public static final int GLUE_LEFT = ION2016.LEFT;
	public static final int GLUE_RIGHT = ION2016.RIGHT;
	public static final int GLUE_NONE = Integer.MAX_VALUE;
	
	/**
	 * Makes a new line follower with the specified information
	 *
	 * @param p
	 *            the proportional constant
	 * @param i
	 *            the integral constant
	 * @param d
	 *            the derivative constant
	 * @param pilot
	 *            the differential pilot used to control the robot
	 * @param lineMonitor
	 *            the line monitor that provides sensor data
	 */
	public LineFollower(double p, double i, double d, DifferentialPilot pilot, LineMonitor lineMonitor) {
	
		this.p = p;
		this.i = i;
		this.d = d;
		this.pilot = pilot;
		this.lineMonitor = lineMonitor;
		
		lineFollower = new UtilityDaemon(new LineDaemon());
		angularCalculator = new DerivativeController(1, Double.POSITIVE_INFINITY);
		dAECalculator = new DerivativeController(1, Double.POSITIVE_INFINITY);
		whiteSpeed = ION2016.courseConstants.get("whiteSpeed").getValueIn(Unit.METER_PER_SECOND);
		yellowSpeed = ION2016.courseConstants.get("yellowSpeed").getValueIn(Unit.METER_PER_SECOND);
	}
	
	/**
	 * Makes the robot start following the line.
	 */
	public void startLineFollowing() {
	
		ION2016.report("Start line following called");
		lineFollower.startDaemon();
	}
	
	/**
	 * Makes the robot stop following the line but does not stop the robot.
	 */
	public void stopLineFollowing() {
	
		lineFollower.pauseDeamon();
		ION2016.report("Line Following Paused");
	}
	
	/**
	 * Permanently terminates the line following thread. If {@link #startLineFollowing()} or {@link #stopLineFollowing()} is called after this, an
	 * exception will be thrown.
	 */
	public void terminateLineFollowingThread() {
	
		lineFollower.terminateDeamon();
	}
	
	/**
	 * Returns true if currently line following.
	 *
	 * @return true if currently line following.
	 */
	public boolean isLineFollowing() {
	
		return lineFollower.isRunning();
	}
	
	/**
	 * @return the p
	 */
	public double getP() {
	
		return p;
	}
	
	/**
	 * @param p
	 *            the p to set
	 */
	public void setP(double p) {
	
		this.p = p;
	}
	
	/**
	 * @return the i
	 */
	public double getI() {
	
		return i;
	}
	
	/**
	 * @param i
	 *            the i to set
	 */
	public void setI(double i) {
	
		this.i = i;
	}
	
	/**
	 * @return the d
	 */
	public double getD() {
	
		return d;
	}
	
	/**
	 * @param d
	 *            the d to set
	 */
	public void setD(double d) {
	
		this.d = d;
	}
	
	/**
	 * @return the pilot
	 */
	public DifferentialPilot getPilot() {
	
		return pilot;
	}

	/**
	 * Sets the max speed.
	 *
	 * @param speed
	 *            the new max speed in m/s.
	 */
	public void setMaxSpeed(double speed) {
	
		maxSpeed = speed;
	}
	
	/**
	 * Gets the max speed.
	 *
	 * @return the max speed, in m/s.
	 */
	public double getSpeed() {
	
		return maxSpeed;
	}
	
	public void setGlueState(int side) {

		switch (side) {
		case GLUE_LEFT:
			synchronized (glueLock) {
				glueLeft = true;
				glueRight = false;
			}
			break;
		case GLUE_RIGHT:
			synchronized (glueLock) {
				glueLeft = false;
				glueRight = true;
			}
			break;
		case GLUE_NONE:
			synchronized (glueLock) {
				glueLeft = false;
				glueRight = false;
			}
			break;
		default:
			throw new IllegalArgumentException("setGlueState side was :" + side);
		}
	}

	/**
	 * @return the glueValue
	 */
	public double getGlueValue() {
	
		return glueValue;
	}
	
	/**
	 * The class that does all the line following
	 *
	 * @author jacob
	 *
	 */
	private class LineDaemon implements Runnable {
		
		/**
		 *
		 */
		@Override
		public void run() {
		
			Object[] blendDataLeft = lineMonitor.getAllBlendData(ION2016.LEFT);
			Object[] blendDataRight = lineMonitor.getAllBlendData(ION2016.RIGHT);
			
			double errorLeft = 2 * ((double) blendDataLeft[1] - .5);
			double errorRight = 2 * ((double) blendDataRight[1] - .5);
			synchronized (glueLock) {
				if (glueRight) {
					errorLeft = glueValue;
					errorRight *= 2;
				}
				if (glueLeft) {
					errorRight = glueValue;
					errorLeft *= 2;
				}
			}

			// System.out.printf("%.2f, %.2f\n", errorLeft, errorRight);
			
			double posError = errorLeft - errorRight;
			double angularError = -angularCalculator.getOutput(posError, 0);
			double derAE = -dAECalculator.getOutput(angularError, 0);
			posError = error(errorLeft, errorRight, .5);

			// A P dE
			// System.out.printf("%.2f, %.2f, %.2f\n", angularError, error(errorLeft, errorRight, .5), derAE);
			// System.out.println(correction.toString(8, Unit.RECIPROCAL_METER));
			// double totalError = -(angularError * p + posError * i + derAE * d);

			// ION.report(String.format("%.2f", totalError));
			
			if (Button.UP.isDown() && Button.DOWN.isDown()) {
				ION2016.report("LFCR Started");// Line following calibration routine
				while (Button.ESCAPE.isUp()) {
					ION2016.report("PID");
					if (Button.LEFT.isDown())
						p = ION2016.interactiveSet("P", p);
					else if (Button.UP.isDown())
						i = ION2016.interactiveSet("I", i);
					else if (Button.RIGHT.isDown())
						d = ION2016.interactiveSet("D", d);
				}
				ION2016.courseConstants.put("lineFollowingP", new Quantity(p, Unit.ONE));
				ION2016.courseConstants.put("lineFollowingI", new Quantity(i, Unit.ONE));
				ION2016.courseConstants.put("lineFollowingD", new Quantity(d, Unit.ONE));
				
			}
			
			pilot.steer(-(angularError * p + posError * i + derAE * d));
			// TODO Check speed limits
			// String colorLeft = (String) blendDataLeft[0];
			// String colorRight = (String) blendDataRight[0];
			// if (colorLeft.equals(colorRight) && colorLeft.equals("white"))
			// pilot.setTravelSpeed(whiteSpeed);
			// else
			// double k = .2;
			// double speedCorrection = (1 - k) * Math.exp(-Math.pow(posError / .6, 4)) + k;
			
			double maxSpeedCorrection = (whiteSpeed - .2 * whiteSpeed) / 2 * Math.abs(posError) + whiteSpeed;
			maxSpeedCorrection = Math.min(maxSpeedCorrection, yellowSpeed);
			
			//ION2016.report("MSC: %.2f", maxSpeedCorrection);

		//	ION2016.debug(1, "Max Speed: %.2f", maxSpeed);
		//	ION2016.debug(1, "Yellow Speed: %.2f", yellowSpeed);
			double nextSpeed = Math.min(maxSpeed, yellowSpeed);
			if (pilot.getTravelSpeed() != nextSpeed)
				pilot.setTravelSpeed(nextSpeed);
		//	ION2016.debug(1, "Next Speed: %.2f", nextSpeed);

		}

	}

	/**
	 * Returns the result of the trust function.
	 *
	 * @param x
	 *            the value to trust.
	 * @param k
	 *            the parameter.
	 * @return the result.
	 */
	private static double trust(double x, double k) {
	
		return 1 - 1 / (1 + Math.exp(-x / k));
	}

	/**
	 * Calculates the total error.
	 *
	 * @param l
	 *            the left error.
	 * @param r
	 *            the right error.
	 * @param k
	 *            the tuning parameter.
	 * @return the total error.
	 */
	public static double error(double l, double r, double k) {
	
		double trustL = trust(l, k);
		double trustR = trust(r, k);

		return 2 / (trustL + trustR) * (l * trustL - r * trustR);
	}
}