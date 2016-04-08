package main;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author jacob
 *
 */
public class SimpleCourseController implements CourseController {
	
	/**
	 * The iterator used to get the next actions
	 */
	private final Iterator<RobotAction> iter;
	
	/**
	 * Make a new simple course controller with the specified information.
	 *
	 * @param actions
	 *            the actions to make the robot do.
	 */
	public SimpleCourseController(List<RobotAction> actions) {
	
		iter = actions.iterator();
	}

	/**
	 * Does the same thing as {@link #SimpleCourseController(List)}.
	 *
	 * @param actions
	 *            the actions to make the robot do.
	 */
	public SimpleCourseController(RobotAction... actions) {

		this(Arrays.asList(actions));
	}

	/**
	 * Called to inform the course controlled of an event and allow the course controller to decide the next action.
	 *
	 * @param event
	 *            the event to inform the course controller of.
	 */
	@Override
	public void logEvent(RobotMessage event) {
	
		// TODO Store the event, and use it to determine the robot's current position.
		// The event will be one of the following:
		// RobotMessage.INTERSECTION_DETECTED
		// RobotMessage.INTERSECTION_NAVIGATED
		// RobotMessage.PARKING_LOT_LEFT_DETECTED
		// RobotMessage.PARKING_LOT_RIGHT_DETECTED
		// RobotMessage.PARKED
		// RobotMessage.PULLED_OUT
	}

	/**
	 * Gets the next action.
	 *
	 * @return the action the robot should do next. If there are no more actions (the robot has finished), this method should return a
	 *         {@link CelebrateAction}.
	 */
	@Override
	public RobotAction getNextAction() {

		if (iter.hasNext())
			return iter.next();
		else
			return new CelebrateAction(-1);
	}
}