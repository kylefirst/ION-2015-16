package utilities.task;

/**
 * This class is used for monitoring a task. When a class executes a action asynchronously, it can provide the client with a {@link TaskMonitor}. The
 * {@link TaskMonitor} will allow the client to monitor the state of the task being performed. The class which is performing the action is expected to
 * call {@link #setState(STATE)} in order to inform the {@link TaskMonitor} of events. The client should use {@link #getState()},
 * {@link #waitForState(STATE)} and {@link #waitForStateNot(STATE)} in order to moitor the task.
 * 
 * @author Jacob Glueck
 * 
 */
public class TaskMonitor {
	
	/**
	 * Represents the current state.
	 */
	private STATE state;
	
	/**
	 * The object used for locking state.
	 */
	private final Object stateLock;
	
	/**
	 * Makes a new Task Monitor. The state is set to {@link STATE#READY}.
	 */
	public TaskMonitor() {
	
		state = STATE.READY;
		stateLock = new Object();
	}
	
	/**
	 * Gets the current state.
	 * 
	 * @return The state.
	 */
	public STATE getState() {
	
		return state;
	}
	
	/**
	 * Sets the current state. This method should be used by the object executing the task.
	 * 
	 * @param state
	 *            The state to set.
	 */
	public void setState(STATE state) {
	
		synchronized (stateLock) {
			
			// Only notify waiting threads if the state changed.
			if (this.state != state) {
				this.state = state;
				stateLock.notifyAll();
			}
		}
	}
	
	/**
	 * Waits for the state to equal the target state.
	 * 
	 * @param targetState
	 *            The target state.
	 */
	public void waitForState(STATE targetState) {
	
		synchronized (stateLock) {
			while (state != targetState)
				try {
					stateLock.wait();
				} catch (InterruptedException e) {
				}
		}
	}
	
	/**
	 * Waits for the state change from the parameter.
	 * 
	 * @param notTargetState
	 *            The state that is not the target state.
	 * @return Returns the state after is has changed from the notTargetState.
	 */
	public STATE waitForStateNot(STATE notTargetState) {
	
		STATE finalState;
		synchronized (stateLock) {
			while ((finalState = state) == notTargetState)
				try {
					stateLock.wait();
				} catch (InterruptedException e) {
				}
		}
		return finalState;
	}
}