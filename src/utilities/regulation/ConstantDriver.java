package utilities.regulation;

/**
 * This class represents a driver that provides a constant output.
 * 
 * @author Jacob Glueck
 */
public class ConstantDriver implements Driver {
	
	/**
	 * The constant output.
	 */
	private final double output;
	
	/**
	 * Makes a new ConstantDriver with the specified information.
	 * 
	 * @param output
	 *            The constant output.
	 * 
	 */
	public ConstantDriver(double output) {
	
		this.output = output;
	}
	
	/**
	 * @return The constant output.
	 */
	@Override
	public double getTarget() {
	
		return output;
	}
	
	/**
	 * @return False.
	 */
	@Override
	public boolean moveComplete(double currentValue) {
	
		return false;
	}
}