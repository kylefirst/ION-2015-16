package utilities.units;

import java.util.Arrays;

/**
 * Represents a unit and provides constants for standard units.
 *
 * @author Jacob Glueck
 */
public class Unit {

	// ########################################// Start common units
	/**
	 * Represents a quantity without units.
	 */
	public static final Unit ONE = new Unit();
	// ========================================//Time units
	/**
	 * Represents a second.
	 */
	public static final Unit SECOND = new Unit(BASE_UNIT.SECOND);
	/**
	 * Represents a millisecond.
	 */
	public static final Unit MILLISECOND = new Unit(Unit.SECOND, 0.001);
	/**
	 * Represents a minute.
	 */
	public static final Unit MINUTE = new Unit(Unit.SECOND, 60);
	// =======================================//Length units
	/**
	 * Represents a meter.
	 */
	public static final Unit METER = new Unit(BASE_UNIT.METER);
	/**
	 * Represents a centimeter.
	 */
	public static final Unit CENTIMETER = new Unit(Unit.METER, .01);
	/**
	 * Represents a millimeter.
	 */
	public static final Unit MILLIMETER = new Unit(Unit.METER, .001);
	// ========================================//Angle units
	/**
	 * Represents degrees.
	 */
	public static final Unit DEGREE = new Unit(BASE_UNIT.DEGREE);
	/**
	 * Represents radians.
	 */
	public static final Unit RADIAN = new Unit(Unit.DEGREE, 180 / Math.PI);
	/**
	 * Represents revolutions
	 */
	public static final Unit REVOLUTION = new Unit(Unit.DEGREE, 360);
	// ========================================//Curvature units
	/**
	 * Represents a reciprocal meter.
	 */
	public static final Unit RECIPROCAL_METER = Unit.getReciprocal(Unit.METER);
	/**
	 * Represents a reciprocal centimeter.
	 */
	public static final Unit RECIPROCAL_CENTIMETER = Unit.getReciprocal(Unit.CENTIMETER);
	// ========================================//Linear velocity units
	/**
	 * Represents a meter per second.
	 */
	public static final Unit METER_PER_SECOND = Unit.divide(Unit.METER, Unit.SECOND);
	/**
	 * Represents a centimeter per second.
	 */
	public static final Unit CENTIMETER_PER_SECOND = Unit.divide(Unit.CENTIMETER, Unit.SECOND);
	// ========================================//Linear acceleration units
	/**
	 * Represents a meter per square second.
	 */
	public static final Unit METER_PER_SQUARE_SECOND = Unit.divide(Unit.METER_PER_SECOND, Unit.SECOND);
	/**
	 * Represents a centimeter per square second.
	 */
	public static final Unit CENTIMETER_PER_SQUARE_SECOND = Unit.divide(Unit.CENTIMETER_PER_SECOND, Unit.SECOND);
	// ========================================//Angular velocity units
	/**
	 * Represents a degree per second.
	 */
	public static final Unit DEGREE_PER_SECOND = Unit.divide(Unit.DEGREE, Unit.SECOND);
	/**
	 * Represents a radian per second.
	 */
	public static final Unit RADIAN_PER_SECOND = Unit.divide(Unit.RADIAN, Unit.SECOND);
	/**
	 * Represents a revolution per second.
	 */
	public static final Unit REVOLUTION_PER_SECOND = Unit.divide(Unit.REVOLUTION, Unit.SECOND);
	// ========================================//Angular acceleration units
	/**
	 * Represents a degree per square second.
	 */
	public static final Unit DEGREE_PER_SQUARE_SECOND = Unit.divide(Unit.DEGREE_PER_SECOND, Unit.SECOND);
	/**
	 * Represents a radian per square second.
	 */
	public static final Unit RADIAN_PER_SQUARE_SECOND = Unit.divide(Unit.RADIAN_PER_SECOND, Unit.SECOND);
	/**
	 * Represents a revolution per square second.
	 */
	public static final Unit REVOLUTION_PER_SQUARE_SECOND = Unit.divide(Unit.REVOLUTION_PER_SECOND, Unit.SECOND);
	// ########################################// End common units

	/**
	 * Stores the exponents for each base unit. To get the exponent for a {@link BASE_UNIT} u, use the following code:
	 * <code>exponents[u.ordinal()]</code>.
	 */
	private final double[] exponents;
	/**
	 * Stores the coefficient.
	 */
	private final double coefficient;

	/**
	 * Makes a new unit which is related to an existing unit. For example, the following code would make a nanometer:
	 * <code>new Unit(Unit.METER, 10E-9)</code>/
	 *
	 * @param u
	 *            The current unit.
	 * @param coefficient
	 *            The coefficient.
	 */
	public Unit(Unit u, double coefficient) {

		this(u.exponents, u.coefficient * coefficient);
	}

	/**
	 * Makes a new dimensionless unit.
	 */
	private Unit() {

		this(new double[BASE_UNIT.values().length], 1);
	}

	/**
	 * Makes a new {@link Unit} out of the specified {@link BASE_UNIT}.
	 *
	 * @param u
	 *            The {@link BASE_UNIT}.
	 */
	private Unit(BASE_UNIT u) {

		this(new double[BASE_UNIT.values().length], 1);
		exponents[u.ordinal()] = 1;

	}

	/**
	 * Makes a new unit with the specified exponents and coefficient.
	 *
	 * @param exponents
	 *            The exponents.
	 * @param coefficient
	 *            The coefficient.
	 */
	private Unit(double[] exponents, double coefficient) {

		this.exponents = exponents;
		this.coefficient = coefficient;
	}

	/**
	 * Returns the conversion to standard units.
	 *
	 * @return the conversion to standard units.
	 */
	public double getConversionToStandardUnits() {

		return coefficient;
	}

	/**
	 * Calculates the conversion factor to get to the specified units.
	 *
	 * @param toBeConvertedTo
	 *            The unit to be converted to.
	 * @return The conversion factor to get the the specified units.
	 */
	public double getConversionToUnits(Unit toBeConvertedTo) {

		if (!isCompatible(toBeConvertedTo))
			throw new IncompatableUnitsEcxeption(this, toBeConvertedTo);

		return coefficient / toBeConvertedTo.coefficient;
	}

	/**
	 * Returns true if the dimensions of the calling object and the parameter are equal.
	 *
	 * @param other
	 *            The units to compare the calling object to.
	 * @return True if the calling object and the parameter have the same dimensions.
	 */
	public boolean isCompatible(Unit other) {

		return Arrays.equals(exponents, other.exponents);
	}

	/**
	 * Returns a string representation.
	 */
	@Override
	public String toString() {

		String symbol = coefficient + " * ";
		for (int x = 0; x < exponents.length; x++)
			symbol += BASE_UNIT.values()[x] + "^" + exponents[x];

		return symbol;
	}

	/**
	 * Generates a hash code
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(coefficient);
		result = prime * result + (int) (temp ^ temp >>> 32);
		result = prime * result + Arrays.hashCode(exponents);
		return result;
	}
	
	/**
	 * Checks for equality
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit other = (Unit) obj;
		if (Double.doubleToLongBits(coefficient) != Double.doubleToLongBits(other.coefficient))
			return false;
		if (!Arrays.equals(exponents, other.exponents))
			return false;
		return true;
	}
	
	/**
	 * Multiplies the two units and returns the result.
	 *
	 * @param u1
	 *            The first unit.
	 * @param u2
	 *            The second unit.
	 * @return The unit created by multiplying u1 and u1 (u1*u2).
	 */
	public static Unit multiply(Unit u1, Unit u2) {

		double[] exponents = new double[BASE_UNIT.values().length];
		for (int x = 0; x < exponents.length; x++)
			exponents[x] = u1.exponents[x] + u2.exponents[x];
		return new Unit(exponents, u1.coefficient * u2.coefficient);
	}

	/**
	 * Divides the two units and returns the result.
	 *
	 * @param u1
	 *            The dividend.
	 * @param u2
	 *            The divisor.
	 * @return The unit created by dividing u1 by u2 (u1/u2).
	 */
	public static Unit divide(Unit u1, Unit u2) {

		return Unit.multiply(u1, Unit.getReciprocal(u2));
	}

	/**
	 * Gets the reciprocal of a unit.
	 *
	 * @param u
	 *            The unit.
	 * @return The reciprocal of the unit.
	 */
	public static Unit getReciprocal(Unit u) {

		double[] exponents = new double[BASE_UNIT.values().length];
		for (int x = 0; x < exponents.length; x++)
			exponents[x] = -u.exponents[x];
		return new Unit(exponents, 1 / u.coefficient);
	}

	/**
	 * Raises the specified unit to the specified power.
	 *
	 * @param u
	 *            The unit.
	 * @param exponent
	 *            The exponent.
	 * @return The unit raised to the exponent.
	 */
	public static Unit pow(Unit u, double exponent) {

		double[] exponents = new double[BASE_UNIT.values().length];
		for (int x = 0; x < exponents.length; x++)
			exponents[x] = u.exponents[x] * exponent;
		return new Unit(exponents, Math.pow(u.coefficient, exponent));
	}

	/**
	 * Represents the base units.
	 *
	 * @author Jacob Glueck
	 */
	private enum BASE_UNIT {
		/**
		 * Represents a meter.
		 */
		METER,
		/**
		 * Represents a second.
		 */
		SECOND,
		/**
		 * Represents a degree (angle).
		 */
		DEGREE;
	}
}
