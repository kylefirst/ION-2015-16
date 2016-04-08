package utilities;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class represents a palette of colors and is intended for use with color matching.
 *
 * @author Tom Casey Rushad Antia
 */
public class ColorPalette {

	/**
	 * The available colors.
	 */
	private final Map<String, Color> colorMap;

	/**
	 * Makes a new empty palette.
	 */
	public ColorPalette() {

		colorMap = new TreeMap<>();
	}

	/**
	 * Adds the specified colors to the palette.
	 *
	 * @param c
	 *            The color to put into the palette.
	 * @param name
	 *            the name of the color in the palette.
	 */
	public void add(Color c, String name) {
	
		if (c == null)
			throw new IllegalArgumentException("Colors cannot be null!");
		colorMap.put(name, c);
	}

	/**
	 * Gets the colors in the palette.
	 *
	 * @return The color map.
	 */
	public Map<String, Color> getColors() {

		return colorMap;
	}
	
	/**
	 * Gets the color with the specified name.
	 *
	 * @param name
	 *            the name of the color to get.
	 * @return the color with the specified name.
	 */
	public Color getColor(String name) {

		return colorMap.get(name);
	}

	/**
	 * Finds the best color match in the palette.
	 *
	 * @param color
	 *            The color to match.
	 * @return The best match or, if there is not a good match, an empty string. In the event of a tie, the first match will be
	 *         returned.
	 */
	public String getBestMatch(Color color) {
	
		double bestDistance = Double.POSITIVE_INFINITY;
		String bestKey = "";

		for (String key : colorMap.keySet()) {
			Color toTest = colorMap.get(key);
			if (isInRange(color, toTest)) {
				double distance = Color.getDistance(toTest, color);
				if (distance < bestDistance) {
					bestKey = key;
					bestDistance = distance;
				}
			}
			
		}

		return bestKey;
	}
	
	/**
	 * Checks to see if the color toTest is close enough to the color rgb.
	 *
	 * @param toTest
	 *            the color to test.
	 * @param rgb
	 *            the target color.
	 * @return true if toTest is close enough to rgb.
	 */
	private boolean isInRange(Color toTest, Color rgb) {
	
		return isInRange(toTest.getRed(), rgb.getRed()) && isInRange(toTest.getGreen(), rgb.getGreen()) && isInRange(toTest.getBlue(), rgb.getBlue());
	}
	
	/**
	 * Checks to see if two numbers are close enough
	 *
	 * @param x
	 *            the first number.
	 * @param y
	 *            the second number.
	 * @return true if the two numbers are close enough.
	 */
	private boolean isInRange(double x, double y) {

		// TODO externalize this!
		return Math.abs(x - y) <= .137254902;
	}
	
	/**
	 * This function helps determine the composition of toTest. toTest is taken to be a combination of two colors, and this function determines which
	 * two colors, when combined, will best create toTest. The color combinations are represent by the set of colors and blended with. Each color in
	 * colors is paired with blendedWith to create a pair of colors which might have created toTest. Each pair of colors creates a line in RGB space,
	 * and all combinations of these two colors lie on this line. This function calculates the distance between toTest and each line, and find the
	 * line that is closest to toTest. In then returns the color in colors that, paired with blendedWith, created the closest line.
	 *
	 * Remember that the blend does not have to be 50-50. To determine the ratio of the colors involved in the blend, use
	 * {@link Color#determineComposition(Color, Color)}.
	 *
	 *
	 * @param colors
	 *            a set of colors, each to paired with blendedWith.
	 * @param blendedWith
	 *            a color to blend with each color in colors.
	 * @param toTest
	 *            the color to test.
	 * @return the name of the color in colors that, paired with blendedWith, created the closest line. Returns null if colors.size() == 0.
	 */
	public String getBestBlend(Set<String> colors, String blendedWith, Color toTest) {

		double bestDistance = Double.POSITIVE_INFINITY;
		String bestColor = null;

		for (String color : colors) {
			double distance = pointLineDistance(getColor(color), getColor(blendedWith), toTest);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestColor = color;
			}
		}
		return bestColor;
	}

	/**
	 * Determines the distance between a color and a line formed by two colors.
	 *
	 * @param lineStart
	 *            the color that is the start of the line.
	 * @param lineEnd
	 *            the color that is the end of the line.
	 * @param point
	 *            the point.
	 * @return the minimum distance between the point and the line.
	 */
	private double pointLineDistance(Color lineStart, Color lineEnd, Color point) {
	
		double[] x1 = lineStart.toDoubleArray();
		double[] x2 = lineEnd.toDoubleArray();
		double[] x0 = point.toDoubleArray();
		
		// |(x0 - x1) x (x0 - x2)| / |x2 - x1|
		return vecMag(vecCross(vecSubtract(x0, x1), vecSubtract(x0, x2))) / vecMag(vecSubtract(x2, x1));
		
	}
	
	/**
	 * Subtracts to arrays like vectors.
	 *
	 * @param a
	 *            the first vector
	 * @param b
	 *            the second vector
	 * @return a-b
	 */
	private double[] vecSubtract(double[] a, double[] b) {

		double[] result = new double[a.length];
		for (int x = 0; x < result.length; x++)
			result[x] = a[x] - b[x];
		return result;
	}
	
	/**
	 * Calculates the magnitude of a vector
	 *
	 * @param a
	 *            the vector.
	 * @return the magnitude.
	 */
	private double vecMag(double[] a) {
	
		double result = 0;
		for (double element : a)
			result += Math.pow(element, 2);
		return Math.sqrt(result);
	}
	
	/**
	 * Calculates the cross product between two vectors.
	 *
	 * @param a
	 *            vector a.
	 * @param b
	 *            vector b.
	 * @return a x b.
	 */
	private double[] vecCross(double[] a, double[] b) {

		// a x b = i (a2 * b3 - a3 * b2) + j (a3 * b1 - a1 * b3) + k (a1 * b2 - a2 * b1),
		return new double[] { a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0] };
	}
	
	/**
	 * Returns the number of colors in this color palette.
	 *
	 * @return The number of colors in this color palette.
	 */
	public int numColors() {

		return colorMap.size();
	}

	@Override
	public String toString() {

		return colorMap.toString();
	}
}