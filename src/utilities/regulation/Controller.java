package utilities.regulation;

/**
 * This interface represents a generic controller
 * 
 * @author Jacob Glueck
 * 
 */
public interface Controller {
	
	/**
	 * Calculates the output for a specified error.
	 * 
	 * @param currentValue
	 *            The current value of the system that is being regulated.
	 * @param targetValue
	 *            The target value of the system that is being regulated.
	 * 
	 * @return The calculated output.
	 */
	public double getOutput(double currentValue, double targetValue);
	
	/**
	 * Resets the controller to its initial state. For example, it the controller was a PID controller, the integral term would be reset back to zero.
	 * It is recommended that after a lengthy period during which the controller was inactive that this method be called.
	 */
	public void reset();
	
	/**
	 * Returns true if the system is stalled which means the controller is unable to significantly change the state of the system.
	 * 
	 * @return True if the system is stalled.
	 */
	public boolean isStalled();
}
