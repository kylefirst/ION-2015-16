package utilities.regulation;

/**
 * This class represents the controlled process in the regulation framework.
 * 
 * @author Jacob Glueck
 */
public interface Controllable {
	
	/**
	 * Sets the process to the specified value.
	 * 
	 * @param value
	 *            The value.
	 */
	public void control(double value);
	
	// TODO Use units here?
	
	/**
	 * Safely stops the process.
	 */
	public void halt();
	
}
