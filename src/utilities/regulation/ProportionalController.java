package utilities.regulation;

/**
 * This class represents a proportional controller.
 * 
 * @author Jacob Glueck
 * 
 */
public class ProportionalController implements Controller {
	
	/**
	 * The proportion
	 */
	private double proportion;
	
	/**
	 * Represents the last error value or 0 if {@link #getOutput(double, double)} has not been called since the creation of the controller or the last
	 * call to {@link #reset()}, whichever came most recently.
	 */
	private double lastOutput;
	
	/**
	 * Represents the maximum allowable error before a stall is declared.
	 */
	private double stallThreshold;
	
	/**
	 * Makes a new proportional controller with the specified constant.
	 * 
	 * @param p
	 *            The constant that relates the output to the error
	 * @param stallThreshold
	 *            Represents the maximum allowable error before {@link #isStalled()} will return true.
	 */
	public ProportionalController(double p, double stallThreshold) {
	
		this.stallThreshold = stallThreshold;
		proportion = p;
	}
	
	/**
	 * @return The error multiplied by the constant.
	 */
	@Override
	public double getOutput(double currentValue, double targetValue) {
	
		lastOutput = targetValue - currentValue;
		return proportion * lastOutput;
	}
	
	@Override
	public void reset() {
	
	}
	
	/**
	 * @return True if the last calculated error value is greater that the stall threshold.
	 */
	@Override
	public boolean isStalled() {
	
		// TODO Auto-generated method stub
		return lastOutput > stallThreshold;
	}
	
	/**
	 * Gets the proportion.
	 * 
	 * @return the proportion
	 */
	public double getProportion() {
	
		return proportion;
	}
	
	/**
	 * Sets the proportion.
	 * 
	 * @param proportion
	 *            the proportion to set
	 */
	public void setProportion(double proportion) {
	
		this.proportion = proportion;
	}
	
	/**
	 * Gets the stall threshold. The stall threshold is the maximum output this controller can output before {@link #isStalled()} returns true.
	 * 
	 * @return the stallThreshold
	 */
	public double getStallThreshold() {
	
		return stallThreshold;
	}
	
	/**
	 * Sets the stall threshold. The stall threshold is the maximum output this controller can output before {@link #isStalled()} returns true.
	 * 
	 * @param stallThreshold
	 *            the stallThreshold to set
	 */
	public void setStallThreshold(double stallThreshold) {
	
		this.stallThreshold = stallThreshold;
	}
	
}
