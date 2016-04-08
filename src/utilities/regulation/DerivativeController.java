package utilities.regulation;

/**
 * This class represents a derivative controller.
 *
 * @author Jacob Glueck
 *
 */
public class DerivativeController implements Controller {

	/**
	 * Represents a constant which is used to scale the derivative.
	 */
	private double kd;

	/**
	 * Represents the error from the last time {@link #getOutput(double, double)} was called.
	 */
	private double lastError;

	/**
	 * Represents the time at which {@link #lastError} was recorded or {@link Double#NEGATIVE_INFINITY} if {@link #lastError} has not been recorded.
	 */
	private double lastTime;

	/**
	 * Represents the calculated discrete time derivative of error or 0 if {@link #getOutput(double, double)} has not been called since the creation
	 * of the controller or the last call to {@link #reset()}, whichever came most recently.
	 */
	private double lastOutput;

	/**
	 * Represents the maximum allowable discrete time derivative of error before a stall is declared.
	 */
	private double stallThreshold;

	/**
	 * Makes a new derivative controller with the specified information.
	 *
	 * @param d
	 *            The derivative scaling factor. When {@link #getOutput(double, double)} is called, the discrete time derivative is calculated and
	 *            multiplied
	 *            by the scaling factor.
	 * @param stallThreshold
	 *            The stall threshold; used by the {@link #isStalled()} method. When the discrete time derivative of error is greater that the stall
	 *            threshold, {@link #isStalled()} will return true.
	 *
	 */
	public DerivativeController(double d, double stallThreshold) {

		this.stallThreshold = stallThreshold;
		kd = d;
		reset();
	}

	/**
	 * @return Returns the discrete time derivative of error multiplied
	 *         by the scaling factor. Time is in units of milliseconds.
	 */
	@Override
	public double getOutput(double currentValue, double targetValue) {

		double error = targetValue - currentValue;
		double currentTime = System.currentTimeMillis();
		double output = (error - lastError) / (currentTime - lastTime);
		lastError = error;
		lastTime = currentTime;
		lastOutput = output;
		return output * kd;
	}

	@Override
	public void reset() {

		lastError = 0;
		lastOutput = 0;
		lastTime = Double.NEGATIVE_INFINITY;

	}

	/**
	 * @return True when the discrete time derivative of error is greater that the stall
	 *         threshold.
	 */
	@Override
	public boolean isStalled() {

		return lastOutput > stallThreshold;
	}

	/**
	 * Gets the factor which is multiplied by the derivative.
	 *
	 * @return the factor which is multiplied by the derivative.
	 */
	public double getKD() {

		return kd;
	}

	/**
	 * Sets the factor which is multiplied by the derivative.
	 *
	 * @param d
	 *            The factor which is multiplied by the derivative.
	 */
	public void setKD(double d) {

		kd = d;
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