package main;

/**
 * @author jacob
 *
 */
public interface CourseController {
	
	/**
	 * Called to inform the course controlled of an event and allow the course controller to decide the next action.
	 *
	 * @param event
	 *            the event to inform the course controller of.
	 */
	public void logEvent(RobotMessage event);
	
	/**
	 * Gets the next action.
	 *
	 * @return the action the robot should do next. If there are no more actions (the robot has finished), this method should return a
	 *         {@link CelebrateAction}.
	 */
	public RobotAction getNextAction();
	
}