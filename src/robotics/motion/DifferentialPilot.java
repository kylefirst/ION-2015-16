package robotics.motion;

import lejos.hardware.motor.BaseRegulatedMotor;
import utilities.regulation.Controllable;
import utilities.units.Quantity;
import utilities.units.Unit;

/**
 * A pilot which simplifies control of a robot. There are five types of moves the robot can make. They are:
 * <ol>
 * <li><b>Travel: </b> makes the robot move along a path over a distance.</li>
 * <li><b>Arc: </b> makes the robot move along a circular path over an angle.</li>
 * <li><b>Drive: </b> makes the robot start moving along a path; the robot will keep moving until stop is called.</li>
 * <li><b>Rotate: </b> makes the robot rotate in place over a specified angle.</li>
 * <li><b>Spin: </b> makes the robot start spinning.</li>
 * </ol>
 *
 * @author Jacob Glueck
 */
public class DifferentialPilot implements Controllable {

	/**
	 * The distance between the center of the wheels.
	 */
	private final double trackWidth;
	/**
	 * The wheel radius.
	 */
	private final double wheelRadius;
	/**
	 * The motor on the left.
	 */
	private final BaseRegulatedMotor left;
	/**
	 * The motor on the right.
	 */
	private final BaseRegulatedMotor right;
	/**
	 * The speed.
	 */
	private double speed;
	/**
	 * The acceleration.
	 */
	private double acceleration;
	/**
	 * The rotation speed
	 */
	private double rotateSpeed;
	/**
	 * The rotational acceleration
	 */
	private double rotateAcceleration;
	/**
	 * The parity, to ensure that forwards makes the robot move forwards.
	 */
	private final int parity;
	/**
	 * Normal parity.
	 */
	public static final int NORMAL = 1;
	/**
	 * Forwards.
	 */
	public static final int FORWARDS = 1;
	/**
	 * Counterclockwise.
	 */
	public static final int COUNTERCLOCKWISE = -1;
	/**
	 * Opposite parity.
	 */
	public static final int REVERSE = -1;
	/**
	 * Backwards.
	 */
	public static final int BACKWARDS = -1;
	/**
	 * Clockwise.
	 */
	public static final int CLOCKWISE = 1;
	/**
	 * Represents the right side.
	 */
	private static final int RIGHT = -1;
	/**
	 * Represents the left side.
	 */
	private static final int LEFT = 1;

	/**
	 * Makes a new {@link DifferentialPilot} with the specified information.
	 *
	 * @param trackWidth
	 *            The distance between the centers of the two wheels.
	 * @param wheelDiameter
	 *
	 *            The wheel diameter.
	 * @param left
	 *            The ID of the motor that controls the left wheel should be either <code>'A'</code>, <code>'B'</code> or <code>'C'</code>.
	 * @param right
	 *            The ID of the motor that controls the left wheel should be either <code>'A'</code>, <code>'B'</code> or <code>'C'</code>.
	 */
	public DifferentialPilot(Quantity trackWidth, Quantity wheelDiameter, BaseRegulatedMotor left, BaseRegulatedMotor right) {

		this(trackWidth, wheelDiameter, left, right, DifferentialPilot.NORMAL);
	}

	/**
	 * Makes a new {@link DifferentialPilot} with the specified information.
	 *
	 * @param trackWidth
	 *            The distance between the centers of the two wheels.
	 * @param wheelDiameter
	 *
	 *            The wheel diameter.
	 * @param left
	 *            The ID of the motor that controls the left wheel should be either <code>'A'</code>, <code>'B'</code> or <code>'C'</code>.
	 * @param right
	 *            The ID of the motor that controls the left wheel should be either <code>'A'</code>, <code>'B'</code> or <code>'C'</code>.
	 * @param parity
	 *            If the robot moves backwards when forwards is called, pass in {@link #REVERSE}. Otherwise, you should be using
	 *            {@link #DifferentialPilot(Quantity, Quantity, BaseRegulatedMotor, BaseRegulatedMotor)}.
	 */
	public DifferentialPilot(Quantity trackWidth, Quantity wheelDiameter, BaseRegulatedMotor left, BaseRegulatedMotor right, int parity) {

		this.trackWidth = trackWidth.getValueIn(Unit.METER);
		wheelRadius = wheelDiameter.getValueIn(Unit.METER) / 2;
		this.left = left;
		this.right = right;
		speed = .25;
		acceleration = .5;
		rotateSpeed = 100;
		rotateAcceleration = 100;// 100;
		this.parity = parity;
		zeroDistance();
	}

	// Arc: the robot moves along a path over an angle.
	/**
	 * Moves the robot in an arc with the specified radius over the specified angle.
	 *
	 * @param radius
	 *            The radius of the arc. If positive, the center of the arc will be on the robot's right. If negative, the center of the arc will be
	 *            on the robot's left. May not be 0.
	 * @param angle
	 *            The angle to arc through. Positive angle means the robot will travel forwards, negative means the robot will travel backwards.
	 * @param immediateReturn
	 *            If false, the method will not return until the motor has stopped or stalled.
	 */
	public void arc(Quantity radius, Quantity angle, boolean immediateReturn) {

		double arcRadius = radius.getValueIn(Unit.METER);
		double arcAngle = angle.getValueIn(Unit.DEGREE);

		double curvature = 1 / arcRadius;
		arcAngle *= Math.signum(arcRadius);
		setDriveData(curvature);
		double dRight = calculateArcDistance(arcRadius, arcAngle, DifferentialPilot.RIGHT);
		double dLeft = calculateArcDistance(arcRadius, arcAngle, DifferentialPilot.LEFT);
		activateMove(dRight, dLeft, immediateReturn);
	}

	// Drive: the robot moves along a path indefinitely.
	/**
	 * Starts the robot moving forwards.
	 */
	public void forwards() {

		drive(new Quantity(0, Unit.RECIPROCAL_METER));
	}

	/**
	 * Starts the robot moving backwards.
	 */
	public void backwards() {

		drive(new Quantity(0, Unit.RECIPROCAL_METER), DifferentialPilot.BACKWARDS);
	}

	/**
	 * Starts the robot forwards moving along a path with the specified curvature.
	 *
	 * @param curvature
	 *            The curvature of the robot's path. Curvature is the reciprocal of the radius of curvature. A curvature of zero means the robot will
	 *            go straight. If positive, the center of the arc will be on the robot's right. If negative, the center of the arc will be on the
	 *            robot's left.
	 * */
	public void drive(Quantity curvature) {

		drive(curvature, DifferentialPilot.FORWARDS);
	}

	/**
	 * Makes the robot move along a curve path specified by the turn rate.
	 *
	 * @param turnRate
	 *            A number which indicates how rapidly the robot will turn. A turn rate of 0 will make the robot go straight, a positive turn rate
	 *            will make the robot turn right and a negative turn rate will make the robot turn left.
	 */
	public void drive(double turnRate) {

		left.setSpeed((int) toAngularQuantity(speed * (1 + turnRate)));
		right.setSpeed((int) toAngularQuantity(speed * (1 - turnRate)));
		left.setAcceleration((int) toAngularQuantity(acceleration * (1 + turnRate)));
		right.setAcceleration((int) toAngularQuantity(acceleration * (1 - turnRate)));
		activateMove(Math.signum(1 + turnRate) * Integer.MAX_VALUE, Math.signum(1 - turnRate) * Integer.MAX_VALUE, true);
	}

	/**
	 * Controls the curvature of the robot's path. This method calls {@link #drive(double)} with the value.
	 */
	@Override
	public void control(double value) {

		// System.out.println(value);
		drive(new Quantity(value, Unit.RECIPROCAL_METER));
		// drive(value);
	}

	/**
	 * Stops the pilot.
	 */
	@Override
	public void halt() {

		stop(false);
	}

	/**
	 * Starts the robot moving along a path with the specified curvature.
	 *
	 * @param curvature
	 *            The curvature of the robot's path. Curvature is the reciprocal of the radius of curvature. A curvature of zero means the robot will
	 *            go straight. If positive, the center of the arc will be on the robot's right. If negative, the center of the arc will be on the
	 *            robot's left.
	 * @param direction
	 *            The direction in which to travel. Should be either {@link #FORWARDS} or {@link #BACKWARDS}.
	 */
	public void drive(Quantity curvature, int direction) {

		travel(new Quantity(Integer.MAX_VALUE * direction, Unit.METER), curvature, true);
	}

	// Travel: the robot moves along a path over a distance.
	/**
	 * Moves the robot over the specified distance with a specified curvature.
	 *
	 * @param distance
	 *            The distance to travel. If positive, the robot will move forwards. If negative, the robot will move backwards.
	 * @param curvature
	 *            The curvature of the robot's path. Curvature is the reciprocal of the radius of curvature. A curvature of zero means the robot will
	 *            go straight. If positive, the center of the arc will be on the robot's right. If negative, the center of the arc will be on the
	 *            robot's left.
	 * @param immediateReturn
	 *            If false, the method will not return until the motor has stopped or stalled.
	 */
	public void travel(Quantity distance, Quantity curvature, boolean immediateReturn) {

		double travelDistance = distance.getValueIn(Unit.METER);
		double travelCurvature = curvature.getValueIn(Unit.RECIPROCAL_METER);

		setDriveData(travelCurvature);
		double dRight = travelDistance * calculateArcCoefficient(travelCurvature, DifferentialPilot.RIGHT);
		double dLeft = travelDistance * calculateArcCoefficient(travelCurvature, DifferentialPilot.LEFT);
		activateMove(dRight, dLeft, immediateReturn);
	}

	/**
	 * Moves the robot over the specified distance. Does not return until the move is complete.
	 *
	 * @param distance
	 *            The distance to travel. If positive, the robot will move forwards. If negative, the robot will move backwards.
	 * @param immediateReturn
	 *            If false, the method will not return until the motor has stopped or stalled.
	 */
	public void travel(Quantity distance, boolean immediateReturn) {

		travel(distance, new Quantity(0, Unit.RECIPROCAL_METER), immediateReturn);
	}

	// Rotate: the robot rotates over an angle.
	/**
	 * Rotates the robot around its center the specified amount.
	 *
	 * @param amount
	 *            The amount to turn. If positive, the robot will rotate clockwise. If negative, the robot will rotate counterclockwise.
	 * @param immediateReturn
	 *            If false, the method will not return until the motor has stopped or stalled.
	 */
	public void rotate(Quantity amount, boolean immediateReturn) {

		double angle = amount.getValueIn(Unit.DEGREE);
		double dLeft = angle * trackWidth * Math.PI / 360;
		double dRight = -dLeft;
		double vBoth = toAngularQuantity(rotateSpeed * trackWidth * Math.PI / 360);
		double aBoth = toAngularQuantity(rotateAcceleration * trackWidth * Math.PI / 360);
		// System.out.println("DP R Speed: " + vBoth + " deg/s");
		// System.out.println("DP R Accel: " + aBoth + " deg/s/s");
		right.setSpeed((int) vBoth);
		left.setSpeed((int) vBoth);
		right.setAcceleration((int) aBoth);
		left.setAcceleration((int) aBoth);
		activateMove(dRight, dLeft, immediateReturn);
	}

	// Spin: the robot rotates indefinitely
	/**
	 * Makes the robot start to spin in the specified direction.
	 *
	 * @param direction
	 *            The direction. Should be either {@link #COUNTERCLOCKWISE} or {@link #CLOCKWISE};
	 */
	public void spin(int direction) {
	
		rotate(new Quantity(Integer.MAX_VALUE * direction, Unit.DEGREE), true);
	}

	// Other motion methods
	/**
	 * Makes the robot stop.
	 *
	 * @param immediateReturn
	 *            If false, the method will not return until the motor has stopped or stalled.
	 */
	public void stop(boolean immediateReturn) {

		right.stop(true);
		left.stop(immediateReturn);
	}

	/**
	 * Calculates the distance the robot has traveled.
	 *
	 * @return The distance the robot has traveled since the last call to {@link #zeroDistance()}, or, if {@link #zeroDistance()} has not been called,
	 *         the distance traveled since the pilot was constructed.
	 */
	public Quantity getDistanceTraveled() {

		return new Quantity(Math.abs((right.getTachoCount() + left.getTachoCount()) * Math.PI * wheelRadius / 360), Unit.METER);
	}

	/**
	 * Resets the distance traveled to zero. When {@link #getDistanceTraveled()} is called, it will return the distance which was traveled after the
	 * last call to {@link #zeroDistance()}.
	 */
	public void zeroDistance() {

		right.resetTachoCount();
		left.resetTachoCount();
	}

	// Getters
	/**
	 * @return the acceleration
	 */
	public Quantity getAcceleration() {

		return new Quantity(acceleration, Unit.METER_PER_SQUARE_SECOND);
	}

	/**
	 * @return the speed
	 */
	public Quantity getSpeed() {

		return new Quantity(speed, Unit.METER_PER_SECOND);
	}

	/**
	 * @return the trackWidth
	 */
	public Quantity getTrackWidth() {

		return new Quantity(trackWidth, Unit.METER);

	}

	/**
	 * @return the wheelRadius
	 */
	public Quantity getWheelRadius() {

		return new Quantity(wheelRadius, Unit.METER);
	}

	/**
	 * @return the rotateSpeed
	 */
	public Quantity getRotateSpeed() {

		return new Quantity(rotateSpeed, Unit.DEGREE_PER_SECOND);
	}

	/**
	 * @return the rotateAcceleration
	 */
	public Quantity getRotateAcceleration() {

		return new Quantity(rotateAcceleration, Unit.DEGREE_PER_SQUARE_SECOND);
	}

	// Setters
	/**
	 * @param rotateAcceleration
	 *            the rotateAcceleration to set
	 */
	public void setRotateAcceleration(Quantity rotateAcceleration) {

		this.rotateAcceleration = rotateAcceleration.getValueIn(Unit.DEGREE_PER_SQUARE_SECOND);
	}

	/**
	 * @param acceleration
	 *            the acceleration to set
	 */
	public void setAcceleration(Quantity acceleration) {

		this.acceleration = acceleration.getValueIn(Unit.METER_PER_SQUARE_SECOND);
	}

	/**
	 * @param speed
	 *            the speed to set
	 */
	public void setSpeed(Quantity speed) {

		this.speed = speed.getValueIn(Unit.METER_PER_SECOND);
	}

	/**
	 * @param rotateSpeed
	 *            the rotateSpeed to set
	 */
	public void setRotateSpeed(Quantity rotateSpeed) {

		this.rotateSpeed = rotateSpeed.getValueIn(Unit.DEGREE_PER_SECOND);
	}

	// Private methods to perform common calculations
	/**
	 * Calculates the arc coefficient.
	 *
	 * @param curvature
	 *            The arc curvature.
	 * @param side
	 *            Indicates the motor that the coefficient is being calculated for. Should be either {@link #LEFT} or {@link #RIGHT}.
	 * @return The arc coefficient.
	 */
	private double calculateArcCoefficient(double curvature, int side) {

		return 1 + trackWidth * side / 2 * curvature;
	}

	/**
	 * Calculates the distance the one side of the robot will move in an arc.
	 *
	 * @param radius
	 *            The arc radius.
	 * @param angle
	 *            The angle over which the robot will arc.
	 * @param side
	 *            The side of the robot that the distance is being calculated for. Should be either {@link #LEFT} or {@link #RIGHT}.
	 * @return The distance that the specified side of the robot will move along the specified arc.
	 */
	private double calculateArcDistance(double radius, double angle, int side) {

		return (radius + trackWidth * side / 2) * Math.PI * 2 * (angle / 360);
	}

	/**
	 * Calculates the accelerations and velocities of each wheel based on the specified curvature.
	 *
	 * @param curvature
	 *            The curvature.
	 */
	private void setDriveData(double curvature) {

		right.setAcceleration((int) toAngularQuantity(acceleration * calculateArcCoefficient(curvature, DifferentialPilot.RIGHT)));
		left.setAcceleration((int) toAngularQuantity(acceleration * calculateArcCoefficient(curvature, DifferentialPilot.LEFT)));
		right.setSpeed((int) toAngularQuantity(speed * calculateArcCoefficient(curvature, DifferentialPilot.RIGHT)));
		left.setSpeed((int) toAngularQuantity(speed * calculateArcCoefficient(curvature, DifferentialPilot.LEFT)));
	}

	/**
	 * Moves both motors the specified distance. Both are assumed to finish at the same time.
	 *
	 * @param rightDistance
	 *            The linear distance the right motor must move.
	 * @param leftDistance
	 *            The linear distance the left motor must move.
	 * @param immediateReturn
	 *            If false, the method will not return until the motor has stopped or stalled.
	 */

	private void activateMove(double rightDistance, double leftDistance, boolean immediateReturn) {

		leftDistance = toAngularQuantity(leftDistance) * parity;
		rightDistance = toAngularQuantity(rightDistance) * parity;
		left.rotate((int) leftDistance, true);
		right.rotate((int) rightDistance, immediateReturn);
	}

	/**
	 * Converts a linear quantity into an angular one. This means that length becomes angle. For example, a meter becomes the angle through which the
	 * wheel must rotate, a meter per second becomes the angular velocity at which the wheel must rotate to achieve the same linear velocity.
	 *
	 * @param linearQuantity
	 *            The linear quantity.
	 * @return The angular quantity.
	 */
	private double toAngularQuantity(double linearQuantity) {

		return linearQuantity * 360 / (wheelRadius * Math.PI * 2);
	}
}
