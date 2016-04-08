/**
 *
 */

package utilities.regulation;

/**
 * This class represents an integral controller which.
 *
 * @author Jacob Glueck
 *
 */
public class IntegralController implements Controller {
	
	/**
	 * Represents a constant which is used to scale the integral.
	 */
	private double ki;
	
	/**
	 * Represents the error from the last time {@link #getOutput(double, double)} was called.
	 */
	private double lastError;
	
	/**
	 * Represents the time at which {@link #lastError} was recorded or {@link Double#NEGATIVE_INFINITY} if {@link #lastError} has not been recorded.
	 */
	private double lastTime;
	
	/**
	 * The value of the trapezoidal approximation of the integral of error with respect to time.
	 */
	private double integral;
	
	/**
	 * Represents the maximum allowable value for the integral before a stall is declared.
	 */
	private double stallThreshold;
	/**
	 * Stores the state of the controller. If, since the controller's construction or the last call to {@link #reset()}, which ever happened most
	 * recently, {@link #getOutput(double, double)} has not been called, this variable will be set to {@link #FIRST_TIME}. Otherwise, it will be set
	 * to {@link #NOT_FIRST_TIME}.
	 */
	private int isFirstTime;
	/**
	 * The smallest value the controller will ever output.
	 */
	private double outputMin;
	/**
	 * The biggest value the controller will ever output.
	 */
	private double outputMax;
	
	/**
	 * Shows that {@link #getOutput(double, double)} has not been called since the controller's construction or the last call to {@link #reset()},
	 * which
	 * ever happened most recently.
	 */
	private static final int FIRST_TIME = 0;
	/**
	 * Shows that {@link #getOutput(double, double)} has been called since the controller's construction or the last call to {@link #reset()}, which
	 * ever
	 * happened most recently.
	 */
	private static final int NOT_FIRST_TIME = 1;
	
	/**
	 * Makes a new integral controller with the specified information.
	 *
	 * @param i
	 *            The integral scaling factor. When {@link #getOutput(double, double)} is called, the trapezoidal approximation of the integral of
	 *            error with respect to time is calculated and multiplied with the scaling factor.
	 * @param stallThreshold
	 *            The stall threshold; used by the {@link #isStalled()} method. When the trapezoidal approximation of the integral of
	 *            error with respect to time is greater that the stall threshold, {@link #isStalled()} will return true.
	 *
	 */
	public IntegralController(double i, double stallThreshold) {
	
		this.stallThreshold = stallThreshold;
		ki = i;
		outputMin = Double.NEGATIVE_INFINITY;
		outputMax = Double.POSITIVE_INFINITY;
		reset();
	}
	
	/**
	 * @return Returns the trapezoidal approximation of the integral of error
	 *         with respect to time multiplied by the scaling factor. Time is in units of milliseconds.
	 */
	@Override
	public double getOutput(double currentValue, double targetValue) {
	
		double error = targetValue - currentValue;
		
		double currentTime = System.currentTimeMillis();
		double newIntegral = (lastError + error) * (currentTime - lastTime) / 2 * isFirstTime * ki + integral;
		
		// double maxGrowth = 2;
		// if (Math.abs((newIntegral - integral) / (currentTime - lastTime)) > maxGrowth) {
		// Sound.beep();
		// newIntegral = integral + Math.signum(newIntegral - integral) + maxGrowth * (currentTime - lastTime);
		// }
		
		// Clamp the integral to prevent windup.
		integral += newIntegral;
		integral = Math.min(Math.max(integral, outputMin), outputMax);
		lastError = error;
		lastTime = currentTime;
		isFirstTime = IntegralController.NOT_FIRST_TIME;
		// integral *= Math.exp(-50 / Math.abs(error));// prevents the integral term from becoming too large.
		// integral *= 1 - 1 / Math.exp(Math.pow(error / 1, 2));
		return integral;
	}
	
	@Override
	public void reset() {
	
		integral = 0;
		lastError = 0;
		lastTime = 0;
		isFirstTime = IntegralController.FIRST_TIME;
	}
	
	/**
	 * @return True when the trapezoidal approximation of the integral of
	 *         error with respect to time is greater that the stall threshold.
	 */
	@Override
	public boolean isStalled() {
	
		return integral > stallThreshold;
	}
	
	/**
	 * Gets the factor which is multiplied by the integral.
	 *
	 * @return the factor which is multiplied by the integral.
	 */
	public double getKI() {
	
		return ki;
	}
	
	/**
	 * Sets the factor which is multiplied by the integral.
	 *
	 * @param i
	 *            The factor which is multiplied by the integral.
	 */
	public void setKI(double i) {
	
		ki = i;
	}
	
	/**
	 * @return the outputMin
	 */
	public double getOutputMin() {
	
		return outputMin;
	}
	
	/**
	 * @param outputMin
	 *            the outputMin to set
	 */
	public void setOutputMin(double outputMin) {
	
		this.outputMin = outputMin;
	}
	
	/**
	 * @return the outputMax
	 */
	public double getOutputMax() {
	
		return outputMax;
	}
	
	/**
	 * @param outputMax
	 *            the outputMax to set
	 */
	public void setOutputMax(double outputMax) {
	
		this.outputMax = outputMax;
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