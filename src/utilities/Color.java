package utilities;

import utilities.io.IO;
import utilities.io.Serializer;

/**
 * Represents a color
 */
public class Color {

	/**
	 * Represents the blue value, in the range [0, 1].
	 */
	private final double blue;
	/**
	 * Represents the green value, in the range [0, 1].
	 */
	private final double green;
	/**
	 * Represents the red value, in the range [0, 1].
	 */
	private final double red;

	/**
	 * Constant representing red.
	 */
	public static final Color RED = new Color(1, 0, 0);
	/**
	 * Constant representing black.
	 */
	public static final Color BLACK = new Color(0, 0, 0);
	/**
	 * Constant representing white.
	 */
	public static final Color WHITE = new Color(1, 1, 1);
	/**
	 * Constant representing blue.
	 */
	public static final Color BLUE = new Color(0, 0, 1);
	/**
	 * Constant representing yellow.
	 */
	public static final Color YELLOW = new Color(1, 1, 0);
	/**
	 * Constant representing green.
	 */
	public static final Color GREEN = new Color(0, 1, 0);

	/**
	 * Makes a new color with the specified color. All of the components should be in the range [0, 1].
	 *
	 * @param red
	 *            The red, in the range [0, 1].
	 * @param green
	 *            The green, in the range [0, 1].
	 * @param blue
	 *            The blue, in the range [0, 1].
	 */
	public Color(double red, double green, double blue) {

		if (red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1)
			throw new IllegalArgumentException("All color components must be in the range [0, 1].");

		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/**
	 * A copy constructor.
	 *
	 * @param c
	 *            The color to copy.
	 */
	public Color(Color c) {

		this(c.red, c.green, c.blue);
	}

	/**
	 * Returns true if the colors are equal
	 *
	 * @param other
	 *            color to check
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object other) {

		Color color = (Color) other;

		return red == color.getRed() && green == color.getGreen() && blue == color.getBlue();
	}

	/**
	 * Gets the blue, a value in the range [0, 1].
	 *
	 * @return Returns the blue.
	 */
	public double getBlue() {

		return blue;
	}

	/**
	 * Gets the green, a value in the range [0, 1].
	 *
	 * @return Returns the green.
	 */
	public double getGreen() {

		return green;
	}

	/**
	 * Gets the red, a value in the range [0, 1].
	 *
	 * @return Returns the red.
	 */
	public double getRed() {

		return red;
	}

	/**
	 * Calculates and returns the magnitude of the color using the distance from the origin.
	 *
	 * @return Returns the distance from the color to the origin.
	 */
	public double magnitude() {

		return Math.sqrt(red * red + green * green + blue * blue);
	}

	/**
	 * Combines this color and the parameter by averaging their values.
	 *
	 * @param c
	 *            The color to combine with this one.
	 * @return The combined color.
	 */
	public Color blend(Color c) {

		return blend(c, .5);
	}

	/**
	 * Combines this color and the parameter using the ratio. Returns this * ratio + c * (1 - ratio).
	 *
	 * @param c
	 *            the color to blend with this color.
	 * @param ratio
	 *            between 0 and 1, inclusive. Uses the formula above.
	 * @return the new color, computed as this * ratio + c * (1 - ratio).
	 */
	public Color blend(Color c, double ratio) {
	
		return new Color(red * ratio + c.red * (1 - ratio), green * ratio + c.green * (1 - ratio), blue * ratio + c.blue * (1 - ratio));
	}
	
	/**
	 * Determines the ratio that c1 and c2 were combined in to produce this color.
	 *
	 * @param c1
	 *            the first color used to make this color
	 * @param c2
	 *            the second color used to make this color
	 * @return the ratio such that c1 * ratio + c2 * (1 - ratio), for each component. Takes the average ratio for each component, to get the best fit.
	 */
	public double determineComposition(Color c1, Color c2) {

		double rr = (red - c2.red) / (c1.red - c2.red);
		if (c1.red - c2.red == 0)
			rr = 1;
		double rg = (green - c2.green) / (c1.green - c2.green);
		if (c1.green - c2.green == 0)
			rg = 1;
		double rb = (blue - c2.blue) / (c1.blue - c2.blue);
		if (c1.blue - c2.blue == 0)
			rb = 1;
		return (rr + rg + rb) / 3;
	}

	/**
	 * Undoes a combine operation. If there are two color objects, <code>c</code> and <code>x</code>, then the following statement should be true:
	 * <code>c.reverseBlend(c.combine(x)).equals(x) == true</code>
	 *
	 * @param c
	 *            The combined color.
	 * @return The color that must be combined with this color to produce c.
	 */
	public Color reverseBlend(Color c) {

		return new Color(2 * c.red - red, 2 * c.green - green, 2 * c.blue - blue);
	}

	/**
	 * Makes a readable string
	 *
	 * @return A string in the format [R, G, B] where R, G, and B are all doubles, in the range [0, 1], with 4 digits after the decimal
	 *         points.
	 */
	@Override
	public String toString() {

		String basic = "[" + Math.round(red * 10000) / 10000.0 + ", " + Math.round(green * 10000) / 10000.0 + ", " + Math.round(blue * 10000)
				/ 10000.0 + "]";
		return basic;
	}

	/**
	 * Reads a color from a string. The string should be formatted like the output from {@link #toString()}.
	 *
	 * @param str
	 *            the string to parse.
	 * @return the color parsed from the string.
	 */
	public static Color parseColor(String str) {
	
		str = str.substring(1);
		str = str.substring(0, str.length() - 1);

		String[] split = str.split(", ");
		if (split.length != 3)
			throw new IllegalArgumentException("Color parse error - format wrong");
		return new Color(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
	}

	/**
	 * Converts a color to a double array in the order RGB.
	 *
	 * @return a double array in the order RGB.
	 */
	public double[] toDoubleArray() {
	
		return new double[] { red, green, blue };
	}

	/**
	 * Treats the colors a points in three dimensional space, using their RGB values as coordinates. Calculates the distance between the two colors in
	 * space.
	 *
	 * @param c1
	 *            The first color
	 * @param c2
	 *            The second color
	 * @return The distance between the colors in space.
	 */
	public static double getDistance(Color c1, Color c2) {

		return Math.sqrt(Math.pow(c1.red - c2.red, 2) + Math.pow(c1.green - c2.green, 2) + Math.pow(c1.blue - c2.blue, 2));
	}

	/**
	 * Gets a color serializer.
	 *
	 * @return The serializer.
	 */
	public static Serializer<Color> serializer() {

		return new Serializer<Color>() {

			@Override
			public byte[] serialize(Color data) {

				return IO.serialize(data.red, data.green, data.blue);
			}

			@Override
			public Color deserialize(byte[] bytes) {

				double[] expanded = IO.deserializeDouble(bytes);
				return new Color(expanded[0], expanded[1], expanded[2]);
			}

			@Override
			public Color[] createResultArray(int length) {

				return new Color[length];
			}
		};
	}
}