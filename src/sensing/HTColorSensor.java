package sensing;

import lejos.hardware.port.I2CException;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.HiTechnicColorSensor;
import lejos.hardware.sensor.SensorMode;
import main.ION2016;

/**
 * @author jacob
 *
 */
public class HTColorSensor extends ColorSensor {
	
	/**
	 * The base sensor that provides the data.
	 */
	private final HiTechnicColorSensor baseSensor;
	
	/**
	 * The mode that gets RGB data.
	 */
	private final SensorMode rgbMode;
	
	/**
	 * @param port
	 *            the port
	 */
	public HTColorSensor(Port port) {

		baseSensor = new HiTechnicColorSensor(port);
		rgbMode = baseSensor.getRGBMode();
	}
	
	/**
	 * gets the raw reading
	 */
	@Override
	public double[] getRawReading() {

		try {
			float[] sample = new float[rgbMode.sampleSize()];
			rgbMode.fetchSample(sample, 0);
			double[] result = new double[sample.length];
			for (int x = 0; x < result.length; x++)
				result[x] = sample[x];
			return result;
		} catch (I2CException e) {
			ION2016.report("I2CE on HT sensor");
			return new double[] { .5, .5, .5 };
		}
	}

	// public static void main(String[] args) {
	//
	// HTColorSensor test = new HTColorSensor(SensorPort.S1);
	//
	// System.out.println("Put it over brack");
	// Button.waitForAnyPress();
	// test.calibrateBlack();
	// System.out.println("Put over hwight");
	// Button.waitForAnyPress();
	// test.calibrateWhite();
	//
	// while (Button.ESCAPE.isUp()) {
	// Button.waitForAnyPress();
	// System.out.println(test.getColor());
	// }
	// }
}
