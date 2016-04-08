package main;

/**
 * Represents a robot action.
 */
public abstract class RobotAction {
	
	/**
	 * The sequence number of the robot action.
	 */
	private final int seqNum;
	
	/**
	 * Makes a new robot action with the specified sequence number.
	 *
	 * @param seqNum
	 *            the sequence number of this action.
	 */
	public RobotAction(int seqNum) {

		this.seqNum = seqNum;
	}

	/**
	 * Returns a string with all the data about this action
	 *
	 * @return a string with all the data about this action.
	 */
	protected abstract String dataToString();

	@Override
	public String toString() {
	
		return "[" + seqNum + "] - " + getClass().getSimpleName() + ": " + dataToString();
	}
}
