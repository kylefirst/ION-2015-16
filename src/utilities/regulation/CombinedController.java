package utilities.regulation;

/**
 * This controller combines many controllers into one by adding the outputs of all of the component controllers.
 * 
 * @author Jacob Glueck
 * 
 */
public class CombinedController implements Controller {
	
	/**
	 * Stores all of the component controllers.
	 */
	private final Controller[] controllers;
	/**
	 * The weights for the controllers.
	 */
	private final double[] weights;
	
	/**
	 * Makes a new combined controller with the specified components. The outputs of the controllers are combined with a weighted average such that
	 * the output from the controller at index <code>i</code> is multiplied by the weight at index <code>i</code>.
	 * 
	 * @param c
	 *            The controllers.
	 * @param weight
	 *            The weights. Should be in the range [0, 1], however there are no restrictions against values outside of this range.
	 * 
	 */
	public CombinedController(Controller[] c, double[] weight) {
	
		controllers = c;
		weights = weight;
	}
	
	/**
	 * Makes a new combined controller with the specified components. The outputs of the component controllers are added to calculate the final
	 * output.
	 * 
	 * @param c
	 *            The controllers.
	 * 
	 */
	public CombinedController(Controller... c) {
	
		this(c, new double[c.length]);
		for (int x = 0; x < weights.length; x++)
			weights[x] = 1.0;
	}
	
	/**
	 * Combines the outputs of the component controllers.
	 */
	@Override
	public double getOutput(double currentValue, double targetValue) {
	
		double output = 0;
		for (Controller c : controllers)
			output += c.getOutput(currentValue, targetValue);
		return output;
	}
	
	/**
	 * Resets all of the component controllers.
	 */
	@Override
	public void reset() {
	
		for (Controller c : controllers)
			c.reset();
	}
	
	/**
	 * @return True if all of the component controllers are stalled.
	 */
	@Override
	public boolean isStalled() {
	
		for (Controller c : controllers)
			if (!c.isStalled())
				return false;
		return true;
	}
	
	/**
	 * Gets the controllers.
	 * 
	 * @return the controllers
	 */
	public Controller[] getControllers() {
	
		return controllers;
	}
	
	/**
	 * Gets the weights.
	 * 
	 * @see #CombinedController(Controller[], double[])
	 * @return the weights
	 */
	public double[] getWeights() {
	
		return weights;
	}
	
}
