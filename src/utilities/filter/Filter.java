package utilities.filter;

/**
 * This represents a filter for signals.
 * 
 * @author Jacob Glueck
 */
public interface Filter {
	
	/**
	 * Adds a value of the function to be filtered.
	 * 
	 * @param value
	 *            The value.
	 */
	public void addValue(double value);
	
	/**
	 * Returns the filtered output.
	 * 
	 * @return The filtered output.
	 */
	public double getFilteredOutput();
}
