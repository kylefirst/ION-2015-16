package utilities.regulation;

/**
 * This class represents a steady state controller.
 * 
 * @author Jacob Glueck
 */
public class SteadyStateController implements Controller {
	
	/**
	 * Represents the coefficients of a polynomial expression.
	 */
	double[] coeficcents;
	
	/**
	 * Represents the last value
	 */
	double lastValue;
	/**
	 * Represents the last target value
	 */
	double lastTarget;
	/**
	 * Represents the target value before last target value
	 */
	double lastValueTarget;
	
	/**
	 * Represents the maximum inefficiency. When {@link #getOutput(double, double)} is first called, it calculates the output based on the target
	 * value, t. Thus, next time {@link #getOutput(double, double)} is called, the current value should be equal to t. If the difference between the
	 * current value and t is greater than the stall threshold, than {@link #isStalled()} will return true.
	 */
	double stallThreshold;
	
	/**
	 * Makes a new steady state controller. The controller returns an output calculated using a polynomial function of the target value; it is an
	 * open loop control system.
	 * 
	 * @param stallThreshold
	 *            Represents the maximum inefficiency. When {@link #getOutput(double, double)} is first called, it calculates the output based on the
	 *            target value, t. Thus, next time {@link #getOutput(double, double)} is called, the current value should be equal to t. If the
	 *            difference between the current value and t is greater than the stall threshold, than {@link #isStalled()} will return true.
	 * 
	 * @param c
	 *            Represents the coefficients of the polynomial used for output computation. The value at spot <code>i</code> represents the
	 *            coefficient of the term of degree <code>i</code>.
	 */
	public SteadyStateController(double stallThreshold, double... c) {
	
		this.stallThreshold = stallThreshold;
		coeficcents = c;
		reset();
	}
	
	/** 
	 * 
	 */
	@Override
	public double getOutput(double currentValue, double targetValue) {
	
		double result = 0;
		for (int i = 0; i < coeficcents.length; i++)
			result += Math.pow(targetValue, i) * coeficcents[i];
		lastValue = currentValue;
		lastValueTarget = lastTarget;
		lastTarget = targetValue;
		return result;
	}
	
	@Override
	public void reset() {
	
		lastValue = 0;
		lastTarget = 0;
		lastValueTarget = 0;
	}
	
	/**
	 * @return True if the difference between the most recent current value and its target (the target value in the second most recent
	 *         {@link #getOutput(double, double)} call) is greater than the stall threshold.
	 */
	@Override
	public boolean isStalled() {
	
		return Math.abs(lastValue - lastValueTarget) > stallThreshold;
	}
	
}
