package sensing;

import utilities.Color;

/**
 * This class represents a generic color sensor.
 *
 * @author jacob
 *
 */
public abstract class ColorSensor {

	/**
	 * The raw reading when the sensor is over black.
	 */
	private double[] black = { 0, 0, 0 };
	/**
	 * The raw reading when the sensor is over white.
	 */
	private double[] white = { 1, 1, 1 };

	/**
	 * The index of red in the raw reading array.
	 */
	public static final int RED = 0;
	/**
	 * The index of green in the raw reading array.
	 */
	public static final int GREEN = 1;
	/**
	 * The index of blue in the raw reading array.
	 */
	public static final int BLUE = 2;
	
	/**
	 * Gets the raw reading.
	 *
	 * @return the raw reading, with the red component at the index indicated by {@link #RED}, the green component at the index indicated by
	 *         {@link #GREEN}, and the blue component at the index indicated by {@link #BLUE}.
	 */
	public abstract double[] getRawReading();
	
	/**
	 * Gets the scaled color from the sensor. Ideally, if the surface is black, this should be (0,0,0). If the surface is red, the value should be
	 * (1,1,1).
	 *
	 * @return the color object representing the current sensor reading.
	 */
	public Color getColor() {
	
		double[] raw = getRawReading();
		double[] calibrated = new double[raw.length];
		for (int x = 0; x < calibrated.length; x++)
			calibrated[x] = Math.min(1, Math.max(0, (raw[x] - black[x]) / (white[x] - black[x])));
		return new Color(calibrated[RED], calibrated[GREEN], calibrated[BLUE]);
		
	}
	
	/**
	 * Calibrates the black. This method should be called with the sensor over black.
	 */
	public void calibrateBlack() {
	
		black = getRawReading();
	}
	
	/**
	 * Calibrates the white. This method should be called with the sensor over white.
	 */
	public void calibrateWhite() {

		white = getRawReading();
	}

	/**
	 * Gets the current black calibration data. This is the raw sensor reading that the sensor should read when it is over black.
	 *
	 * @return the raw black data. The array is of length 3 and in the order RGB. The indices {@link #RED}, {@link #GREEN}, and {@link #BLUE} should
	 *         be used.
	 */
	public double[] getBlack() {

		return black;
	}
	
	/**
	 * Sets the current black calibration data, essentially an artificial calibration.
	 *
	 * @param black
	 *            the raw sensor reading that the sensor should read when it is over black. The array is of length 3 and in the order RGB. The indices
	 *            {@link #RED}, {@link #GREEN}, and {@link #BLUE} should be used.
	 */
	public void setBlack(double[] black) {

		this.black = black;
	}
	
	/**
	 * Gets the current white calibration data. This is the raw sensor reading that the sensor should read when it is over white.
	 *
	 * @return the raw white data. The array is of length 3 and in the order RGB. The indices {@link #RED}, {@link #GREEN}, and {@link #BLUE} should
	 *         be used.
	 */
	public double[] getWhite() {

		return white;
	}
	
	/**
	 * Sets the current white calibration data, essentially an artificial calibration.
	 *
	 * @param white
	 *            the raw sensor reading that the sensor should read when it is over white. The array is of length 3 and in the order RGB. The indices
	 *            {@link #RED}, {@link #GREEN}, and {@link #BLUE} should be used.
	 */
	public void setWhite(double[] white) {

		this.white = white;
	}
}