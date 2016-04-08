package utilities.units;

/**
 * Represents a physical quantity which has both a value and units.
 *
 * @author Jacob Glueck
 *
 */
public class Quantity implements Comparable<Quantity> {

	/**
	 * The value.
	 */
	private final double value;
	/**
	 * The units.
	 */
	private final Unit units;

	/**
	 * Makes a new {@link Quantity} with the specified value and units.
	 *
	 * @param val
	 *            The value.
	 * @param u
	 *            The units.
	 */
	public Quantity(double val, Unit u) {

		value = val;
		units = u;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Quantity o) {

		return Double.compare(getValueIn(o.units), o.value);
	}

	/**
	 * Returns the value of the calling object in the specified units.
	 *
	 * @param u
	 *            The units to return in.
	 * @return The value of the calling object in the specified units.
	 */
	public double getValueIn(Unit u) {

		return value * units.getConversionToUnits(u);
	}

	/**
	 * Gets the units for this quantity.
	 * 
	 * @return the units passed into {@link #Quantity(double, Unit)}.
	 */
	public Unit getUnits() {
	
		return units;
	}

	/**
	 * Returns a string representation consisting of the value and units.
	 */
	@Override
	public String toString() {

		return toString(2, units);
	}

	/**
	 * Returns a string representation consisting of the value, formated to the specified number of decimal places, and units.
	 *
	 * @param numDecimals
	 *            The number of decimal places to have in the output.
	 * @param u
	 *            The units in which the value will be converted to prior to returning
	 * @return The value formated to the specified number of decimal places and the units.
	 */
	public String toString(int numDecimals, Unit u) {

		double num = Math.round(getValueIn(u) * Math.pow(10, numDecimals)) / Math.pow(10, numDecimals);
		return num + " " + u;
	}

	/**
	 * Returns the absolute value of a given {@link Quantity}.
	 *
	 * @param pq
	 *            The {@link Quantity} to calculate the absolute value of.
	 * @return The absolute value of the parameter.
	 */
	public static Quantity absoluteValue(Quantity pq) {

		return new Quantity(Math.abs(pq.value), pq.units);
	}

	/**
	 * Adds two {@link Quantity}s.
	 *
	 * @param pq1
	 *            The first {@link Quantity}.
	 * @param pq2
	 *            The second {@link Quantity}.
	 * @return A new {@link Quantity} created by adding the two parameters.
	 */
	public static Quantity add(Quantity pq1, Quantity pq2) {

		double value = pq1.value + pq2.getValueIn(pq1.units);
		return new Quantity(value, pq1.units);
	}

	/**
	 * Subtracts two {@link Quantity}s.
	 *
	 * @param pq1
	 *            The first {@link Quantity}.
	 * @param pq2
	 *            The second {@link Quantity}.
	 * @return A new {@link Quantity} created by subtracting the second parameter from the first.
	 */
	public static Quantity subtract(Quantity pq1, Quantity pq2) {

		double value = pq1.value - pq2.getValueIn(pq1.units);
		return new Quantity(value, pq1.units);
	}

	/**
	 * Divides a {@link Quantity} by a number.
	 *
	 * @param pq1
	 *            The dividend.
	 * @param pq2
	 *            The divisor.
	 * @return A new {@link Quantity} created by dividing the two parameters.
	 */
	public static Quantity divide(Quantity pq1, double pq2) {

		double value = pq1.value / pq2;
		return new Quantity(value, pq1.units);
	}

	/**
	 * Divides a {@link Quantity} by a number.
	 *
	 * @param pq1
	 *            The dividend.
	 * @param pq2
	 *            The divisor.
	 * @return A new {@link Quantity} created by dividing the two parameters.
	 */
	public static Quantity divide(double pq1, Quantity pq2) {

		double value = pq1 / pq2.value;
		return new Quantity(value, Unit.divide(Unit.ONE, pq2.units));
	}

	/**
	 * Divides two {@link Quantity}s.
	 *
	 * @param pq1
	 *            The dividend.
	 * @param pq2
	 *            The divisor.
	 * @return A new {@link Quantity} created by dividing the two parameters.
	 */
	public static Quantity divide(Quantity pq1, Quantity pq2) {

		double value = pq1.value / pq2.value;
		return new Quantity(value, Unit.divide(pq1.units, pq2.units));
	}

	/**
	 * Multiplies a {@link Quantity} by a number.
	 *
	 * @param pq1
	 *            The {@link Quantity}.
	 * @param pq2
	 *            The number to multiply by.
	 * @return A new {@link Quantity} created by multiplying the two parameters.
	 */
	public static Quantity multiply(Quantity pq1, double pq2) {

		double value = pq1.value * pq2;
		return new Quantity(value, pq1.units);
	}

	/**
	 * Multiplies two {@link Quantity}s.
	 *
	 * @param pq1
	 *            The first {@link Quantity}.
	 * @param pq2
	 *            The second {@link Quantity}.
	 * @return A new {@link Quantity} created by multiplying the two parameters.
	 */
	public static Quantity multiply(Quantity pq1, Quantity pq2) {

		double value = pq1.value * pq2.value;
		return new Quantity(value, Unit.multiply(pq1.units, pq2.units));
	}

	/**
	 * Determines the sign of a given {@link Quantity}.
	 *
	 * @param pq
	 *            The {@link Quantity}.
	 * @return The sign of the {@link Quantity}, either -1, 0 or 1.
	 */
	public static double signum(Quantity pq) {

		return Math.signum(pq.value);
	}
}
