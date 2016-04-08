package sensing;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.port.I2CException;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.SensorMode;
import main.ION2016;

/**
 * Represents the EV3 color sensor.
 *
 * @author jacob
 *
 */
public class EV3ColorSensor extends ColorSensor {

	/**
	 * The base sensor from leJOS.
	 */
	private final lejos.hardware.sensor.EV3ColorSensor baseSensor;
	/**
	 * The sensor mode required to get RGB data.
	 */
	private final SensorMode RGBMode;
	
	/**
	 * Makes a new EV3ColorSensor with the specified port.
	 *
	 * @param port
	 *            The port that the sensor is connected to. Should be from {@link SensorPort}.
	 */
	public EV3ColorSensor(Port port) {

		baseSensor = new lejos.hardware.sensor.EV3ColorSensor(port);
		RGBMode = baseSensor.getRGBMode();
	}
	
	/**
	 *
	 */
	@Override
	public double[] getRawReading() {

		// System.out.println("SROP: " + port);

		try {
			float[] sample = new float[RGBMode.sampleSize()];
			RGBMode.fetchSample(sample, 0);
			double[] result = new double[sample.length];
			for (int x = 0; x < result.length; x++)
				result[x] = sample[x];
			
			// System.out.println("EROP: " + port);
			return result;

		} catch (I2CException e) {
			ION2016.report("I2CE on EV3 sensor");
			Button.waitForAnyPress();
			Sound.beep();
			// System.out.println("EROPE: " + port);
			return new double[] { .5, .5, .5 };
		}
	}
}