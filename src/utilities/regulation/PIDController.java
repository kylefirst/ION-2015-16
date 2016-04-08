package utilities.regulation;

/**
 * This class represents a PID controller.
 * 
 * @author Jacob Glueck
 * 
 */
public class PIDController extends CombinedController {
	
	/**
	 * Makes a new PID controller.
	 * 
	 * @param kp
	 *            The proportional scaling factor.
	 * @param ki
	 *            The integral scaling factor.
	 * @param kd
	 *            The derivative scaling factor.
	 * @param stp
	 *            The proportional stall threshold.
	 * @param sti
	 *            The integral stall threshold.
	 * @param std
	 *            The derivative stall threshold.
	 */
	public PIDController(double kp, double ki, double kd, double stp, double sti, double std) {
	
		super(new ProportionalController(kp, stp), new IntegralController(ki, sti), new DerivativeController(kd, std));
	}
	
	/**
	 * Gets the proportional controller.
	 * 
	 * @return The proportional controller.
	 */
	public ProportionalController getProportionalController() {
	
		return (ProportionalController) getControllers()[0];
	}
	
	/**
	 * Gets the integral controller.
	 * 
	 * @return The integral controller.
	 */
	public IntegralController getIntegralController() {
	
		return (IntegralController) getControllers()[1];
	}
	
	/**
	 * Gets the derivative controller.
	 * 
	 * @return The derivative controller.
	 */
	public DerivativeController getDerivativeController() {
	
		return (DerivativeController) getControllers()[2];
	}
	
}
