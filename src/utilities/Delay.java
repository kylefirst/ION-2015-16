package utilities;

import utilities.units.Quantity;
import utilities.units.Unit;

/**
 * This class provides simple methods to create delays.
 * 
 * @author Jacob Glueck
 * 
 */
public class Delay {
	
	/**
	 * Prevent the class from being initialized.
	 */
	private Delay() {
	
	}
	
	/**
	 * Waits for the specified amount of time and can not be interrupted. If the delay is negative, this method will return instantly.
	 * 
	 * @param delay
	 *            The amount of time to delay for.
	 */
	public static void delay(Quantity delay) {
	
		delay((long) delay.getValueIn(Unit.MILLISECOND));
	}
	
	/**
	 * Waits for the specified amount of time and can be interrupted. If the delay is negative, this method will return instantly.
	 * 
	 * @param delay
	 *            The amount of time to delay for.
	 * @throws InterruptedException
	 *             If the delay is interrupted, this method will propagate the exception up the method stack.
	 */
	public static void delayI(Quantity delay) throws InterruptedException {
	
		delayI((long) delay.getValueIn(Unit.MILLISECOND));
	}
	
	/**
	 * Waits for the specified amount of time and can not be interrupted. If the delay is negative, this method will return instantly.
	 * 
	 * @param mills
	 *            The amount of time to delay for, in milliseconds.
	 */
	public static void delay(long mills) {
	
		// Check to see if the period is legal
		if (mills <= 0)
			return;
		
		// Calculate the end time
		long end = System.currentTimeMillis() + mills;
		do {
			
			// Try to sleep until the end
			try {
				Thread.sleep(mills);
			} catch (InterruptedException ie) {
			}
			mills = end - System.currentTimeMillis();
			
			// Keep going while there is time left to sleep
		} while (mills > 0);
	}
	
	/**
	 * Waits for the specified amount of time and can be interrupted. If the delay is negative, this method will return instantly.
	 * 
	 * @param mills
	 *            The amount of time to delay for, in milliseconds.
	 * @throws InterruptedException
	 *             If the delay is interrupted, this method will propagate the exception up the method stack.
	 */
	public static void delayI(long mills) throws InterruptedException {
	
		if (mills <= 0)
			return;
		Thread.sleep(mills);
	}
	
}
