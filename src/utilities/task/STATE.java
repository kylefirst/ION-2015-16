package utilities.task;

/**
 * This enumerated type represents the possible states of tasks.
 * 
 * @author Jacob Glueck
 * 
 */
public enum STATE {
	/**
	 * Indicates that the task is moving towards completion.
	 */
	IN_PROGRESS,
	/**
	 * Indicates that the task is either not moving to completion or moving to completion slower than is expected. However, the task controller is
	 * still trying to push the task towards completion.
	 */
	STALLED,
	/**
	 * Indicates that the task has been completed.
	 */
	COMPLETED,
	/**
	 * Indicates that the task has stopped moving towards completion and that the motion towards completion will never resume.
	 */
	ABORTED,
	/**
	 * Indicates that the task has stopped moving towards completion and that the motion towards completion will eventually resume.
	 */
	SUSPENDED,
	/**
	 * Indicates that the task has not yet been started but that it will start eventually.
	 */
	READY;
}
