package main;

/**
 * This enum has all the messages that the observable classes might send to the robot controller.
 *
 * @author jacob
 *
 */
public enum RobotMessage {
	
	/**
	 * Sent when an intersection is detected. Will only be sent once per intersection.
	 */
	INTERSECTION_DETECTED,
	/**
	 * Sent when a parking lot on the left is detected. May be sent more than once per parking lot.
	 */
	PARKING_LOT_LEFT_DETECTED,
	/**
	 * Sent when a parking lot on the right is detected. May be sent more than once per parking lot.
	 */
	PARKING_LOT_RIGHT_DETECTED,
	/**
	 * Sent after the robot has pulled into a parking space.
	 */
	PARKED,
	/**
	 * Sent after the robot has exited a parking space.
	 */
	PULLED_OUT,
	/**
	 * Sent after the robot has navigated an intersection.
	 */
	INTERSECTION_NAVIGATED,
	/**
	 * Sent from sonar meaning that an object is in front and will rear end the robot. parameter will be set to the distance in meters.
	 */
	APPROACHING_OBJECT,
	/**
	 * Nothing if front, speed up
	 */
	ALL_CLEAR;
	
	/**
	 * Indicates the severity of a message. Represents the distance in m from the approaching object.
	 */
	private double parameter = 0;
	
	/**
	 * @return the parameter
	 */
	public double getParameter() {

		return parameter;
	}
	
	/**
	 * @param parameter
	 *            the parameter to set
	 */
	public void setParameter(double parameter) {

		this.parameter = parameter;
	}
	
}
