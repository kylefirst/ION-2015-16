package main;

import java.util.concurrent.Callable;

import lejos.hardware.port.Port;
import sensing.ColorSensor;
import sensing.EV3ColorSensor;
import sensing.HTColorSensor;

/**
 * Initializes a color sensor and returns it when done.
 *
 */
public class ColorSensorInitializer implements Callable<ColorSensor> {
	
	/**
	 * The port to use.
	 */
	private final Port port;
	
	private boolean isHTC = false;
	
	/**
	 * Makes a new color sensor initializer with the specified port.
	 *
	 * @param port
	 *            the port to use.
	 */
	public ColorSensorInitializer(Port port,boolean isHTC) {

		this.port = port;
		this.isHTC = isHTC;
	}
	
	/**
	 * Initializes the sensor and returns it when done.
	 */
	@Override
	public ColorSensor call() throws Exception {
	
		if(!isHTC) 
		return  new EV3ColorSensor(port);
		
		else return new HTColorSensor(port);
	}

}