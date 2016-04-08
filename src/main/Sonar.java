package main;

import java.util.Observable;

import lejos.hardware.Button;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import utilities.units.Quantity;
import utilities.units.Unit;

/**
 * Controls the sonar.
 *
 * @author Rushad Antia
 * @author Kirill Stavkohsky
 *         TODO add a thread that checks for rear ending but make SURE that it is paused when some other action is called because then it will goof up
 *         and not work properly
 *
 */
public class Sonar extends Observable {
	
	/**
	 * The motor that rotates the sensor.
	 */
	private final BaseRegulatedMotor sensorMotor;
	/**
	 * The tacho count when the motor is at the zero point.
	 */
	private float zeroPoint;
	/**
	 * The sensor.
	 */
	private final EV3UltrasonicSensor sensor;
	/**
	 * The amount of motor rotations required for a 360 degree rotation of the sensor.
	 */
	private final double gearRatio;
	/**
	 * The sample provider.
	 */
	private final SampleProvider m;
	/**
	 * The daemon that prevents rear ending.
	 */
	private final UtilityDaemon daemon;
	/**
	 * The current rear end prevention state. Will be the last robot message sent.
	 */
	private RobotMessage state;
	/**
	 * The threshold for sending {@link RobotMessage#APPROACHING_OBJECT}.
	 */
	private double alertThreshold = Double.POSITIVE_INFINITY;
	
	/**
	 * Makes a new sonar.
	 *
	 * @param controlMotor
	 *            the motor that controls the sonar rotation
	 * @param sensorPort
	 *            the sensor port that the ultrasonic sensor is plugged into
	 * @param maxAngle
	 *            the maximum number of degrees from the straight position that the sensor will rotate
	 * @param gearRatio
	 *            the number of motor rotations required to rotate the sensor 360 degrees
	 */
	public Sonar(BaseRegulatedMotor controlMotor, Port sensorPort, Quantity maxAngle, Quantity gearRatio) {

		sensorMotor = controlMotor;
		sensor = new EV3UltrasonicSensor(sensorPort);
		this.gearRatio = gearRatio.getValueIn(Unit.ONE);
		m = sensor.getDistanceMode();
		daemon = new UtilityDaemon(new SonarDaemon());
		state = RobotMessage.ALL_CLEAR;
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
	 * Calibrates the middle of the sensor
	 */
	public void calibrateSonar() {

		try {
			if (sensorMotor == null)
				ION2016.report("Sensor Motor is Null");
			
			zeroPoint = (int) sensorMotor.getPosition();
			
			while (Button.ENTER.isUp()) {
				
				if (Button.LEFT.isDown())
					zeroPoint -= gearRatio;
				else if (Button.RIGHT.isDown())
					zeroPoint += gearRatio;
				
				sensorMotor.rotateTo((int) zeroPoint);
				
			}
		} catch (Exception e) {
			ION2016.report("Something really weird happened!");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets a distance reading at the specified angle.
	 *
	 * @param angle
	 *            the angle, in degrees, from the equilibrium position. Positive is right, negative is left.
	 * @return the distance, in meters.
	 */
	public float distanceAt(int angle) {

		angle *= -1;
		double target = gearRatio * angle + zeroPoint;
		sensorMotor.rotateTo((int) Math.round(target));
		
		float[] sample = new float[m.sampleSize()];
		m.fetchSample(sample, 0);
		//ION2016.debug(10, "Value: %.4f", sample[0]);
		return sample[0];
	}
	
	/**
	 * Returns true if everything in the region is greater than threshold away.
	 *
	 * @param startAngle
	 *            the start angle, in degrees, from the equilibrium position. Positive is right, negative is left.
	 * @param endAngle
	 *            the end angle, in degrees, from the equilibrium position. Positive is right, negative is left.
	 * @param threshold
	 *            the distance required to return false.
	 * @param increment
	 *            the increment in degrees. Should be positive.
	 * @return true if everything in the region is greater than threshold away, false otherwise.
	 */
	public boolean sweep(int startAngle, int endAngle, double threshold, int increment) {
	
		pauseDeamon();

		ION2016.report("1Start: "+startAngle+ " End: "+endAngle);
		
		startAngle = 30;
		endAngle = -30;//perfect sweeping angle
		
		
		ION2016.report("2Start: "+startAngle+ " End: "+endAngle);
		int sign = endAngle - startAngle > 0 ? 1 : -1;

		ION2016.debug(2, "SDS: " + startAngle + ", " + endAngle);

		// Ensures that the next loop will finish
		while ((Math.abs(endAngle - startAngle) + 1) % increment != 0)
			endAngle += sign;

		for (int x = startAngle; x != endAngle + sign; x += sign * increment) {
			
			double dist = distanceAt(x);
			ION2016.debug(3, "SD: %.2f ,%.2f", threshold, dist);
			if (dist < threshold) {
				ION2016.debug(2, "Sweep False, %.2f, %.2f", threshold, dist);
				return false;

			}
		}
		
		ION2016.debug(2, "Sweep True, %.2f", threshold);
		startDaemon();
		return true;
	}
	
	/**
	 * Gets the alert threshold in m.
	 *
	 * @return the alertThreshold
	 */
	public double getAlertThreshold() {

		return alertThreshold;
	}
	
	/**
	 * @param alertThreshold
	 *            the alertThreshold to set in m.
	 */
	public void setAlertThreshold(double alertThreshold) {

		this.alertThreshold = alertThreshold;
	}
	
	/**
	 * Background thread that prevents rear ending.
	 *
	 */
	class SonarDaemon implements Runnable {
		
		/**
		 * Runs!
		 */
		@Override
		public void run() {
		
			double distance = distanceAt(0);
			boolean shouldNofity = distance != RobotMessage.APPROACHING_OBJECT.getParameter();
			RobotMessage.APPROACHING_OBJECT.setParameter(distance);
			
			// Notify only if distance changes
			if (distance < alertThreshold && shouldNofity) {
				ION2016.report("Nofity approach!");
				notifyObservers(RobotMessage.APPROACHING_OBJECT);
				state = RobotMessage.APPROACHING_OBJECT;
				setChanged();
			} else if (distance >= alertThreshold && state != RobotMessage.ALL_CLEAR) {
				ION2016.report("Nofity clear!");
				notifyObservers(RobotMessage.ALL_CLEAR);
				state = RobotMessage.ALL_CLEAR;
				setChanged();
			}
			
		}
		
	}
	
}
