package main;

/**
 * Represents a celebrate action which the robot will do once it finishes the course.
 */
public class CelebrateAction extends RobotAction {
	
	/**
	 * Makes a new celebrate action.
	 * 
	 * @param seqNum
	 *            the sequence number.
	 */
	public CelebrateAction(int seqNum) {
	
		super(seqNum);
	}

	/**
	 *
	 */
	@Override
	protected String dataToString() {
	
		return "All Done!";
	}
	
}
