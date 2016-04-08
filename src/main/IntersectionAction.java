package main;

/**
 * Represents a new intersection action.
 */
public class IntersectionAction extends RobotAction {

	/**
	 * The angle to turn. If positive, the robot will rotate right. If negative, the robot will rotate left.
	 */
	private final int angle;
	
	/**
	 * Makes a new intersection action.
	 *
	 * @param angle
	 *            the angle to turn. If positive, the robot will rotate right. If negative, the robot will rotate left.
	 * @param seqNum
	 *            the sequence number.
	 */
	public IntersectionAction(int angle, int seqNum) {

		super(seqNum);
		this.angle = angle;
	}

	/**
	 *
	 */
	@Override
	protected String dataToString() {

		return "angle=" + angle;
	}
	
	/**
	 * @return the angle
	 */
	public int getAngle() {

		return angle;
	}
}