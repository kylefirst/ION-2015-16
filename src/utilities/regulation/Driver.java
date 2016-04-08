package utilities.regulation;

/**
 * This class represents the driver component of the regulation framework. The driver calculates the target value in the regulation cycle.
 * 
 * @author Jacob Glueck
 */
public interface Driver {
	
	/**
	 * Gets the target value for the regulation process.
	 * 
	 * @return The target value.
	 */
	public abstract double getTarget();
	
	/**
	 * Returns true if the move is complete.
	 * 
	 * @param currentValue
	 *            The current value.
	 * @return True if the move is complete.
	 */
	public abstract boolean moveComplete(double currentValue);
}
