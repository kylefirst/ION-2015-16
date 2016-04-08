package utilities.regulation;

/**
 * This class represents the monitor component of the regulation framework. The monitor monitors current value of the process being regulated.
 * 
 * @author Jacob Glueck
 */
public interface Monitor {
	
	/**
	 * Gets the current value for the regulation process.
	 * 
	 * @return The current value.
	 */
	public double getCurrentValue();
	
}
