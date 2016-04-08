package utilities.units;

/**
 * The exception thrown when a silly person does something silly with units.
 *
 * @author Jacob Glueck
 */
public class IncompatableUnitsEcxeption extends IllegalArgumentException {

	/**
	 *
	 */
	private static final long serialVersionUID = 8542209356177667363L;
	
	/**
	 * Makes a new {@link IncompatableUnitsEcxeption} and informs the user that they can not convert between the parameters.
	 *
	 * @param u1
	 *            One of the incompatible units.
	 * @param u2
	 *            The other incompatible unit.
	 */
	public IncompatableUnitsEcxeption(Unit u1, Unit u2) {

		super("Can not convert from " + u1 + " to " + u2 + ".");
	}
}
