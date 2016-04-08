package utilities.regulation;

import utilities.task.STATE;
import utilities.task.TaskMonitor;

/**
 * This class represents a regulator which regulates a process defined by a {@link Controllable} object, uses a {@link Driver} object to calculate the
 * target value, a {@link Monitor} for feedback and a {@link Controller} to
 * control the process.
 *
 * @author Jacob Glueck
 *
 */
public class Regulator {
	
	/**
	 * The process that is being controlled.
	 */
	private final Controllable process;
	/**
	 * The controller that is being used to control the process.
	 */
	private final Controller controller;
	/**
	 * The driver that is being used to produce to target values for the
	 * process.
	 */
	private volatile Driver driver;
	/**
	 * The task monitor which is being used to monitor the task. This monitor is informed of all changes in state.
	 */
	private volatile TaskMonitor taskMonitor;
	/**
	 * The object used for locking the driver.
	 */
	private final Object driverLock;
	/**
	 * The monitor that is being used to obtain feedback from the process.
	 */
	private final Monitor monitor;
	/**
	 * True if the regulator is running.
	 */
	private volatile boolean regulationActive;
	/**
	 * The thread the does the regulation.
	 */
	private final RegulationLoop regulationLoop;
	/**
	 * The minimum time for which the controller must remain stalled in order for the driver to be informed of a stall.
	 */
	private double stallTime;
	/**
	 * The logger.
	 */
	private final RegulationLog log;
	/**
	 * If true, logging is active
	 */
	private boolean isLogging;
	
	// Sum of squares of errors, for debugging.
	/**
	 * The current sum of the squares of the error
	 */
	private double sumE2;
	/**
	 * The current number of errors summed in {@link #sumE2}.
	 */
	private int numError;
	
	/**
	 * Makes a new regulator with the specified information. The regulator is
	 * not started until {@link #start()} is called. By default, the stall time is set to {@link Double#POSITIVE_INFINITY}.
	 *
	 * @param process
	 *            The process that is being controlled.
	 * @param controller
	 *            The controller that is being used to control the process.
	 * @param monitor
	 *            The monitor that is being used to obtain feedback from the
	 *            process.
	 * @param stallTime
	 *            The minimum time for which the controller must remain stalled in order for the driver to be informed of a stall.
	 *
	 */
	public Regulator(Controllable process, Controller controller, Monitor monitor, double stallTime) {
	
		// Set the driver to null because there is no driver
		this(process, controller, null, monitor, stallTime);
	}
	
	/**
	 * Makes a new regulator with the specified information. The regulator is
	 * not started until {@link #start()} is called. By default, the stall time is set to {@link Double#POSITIVE_INFINITY}.
	 *
	 * @param process
	 *            The process that is being controlled.
	 * @param controller
	 *            The controller that is being used to control the process.
	 * @param monitor
	 *            The monitor that is being used to obtain feedback from the
	 *            process.
	 *
	 */
	public Regulator(Controllable process, Controller controller, Monitor monitor) {
	
		// Set the driver to null because there is no driver
		this(process, controller, null, monitor, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Makes a new regulator with the specified information. The regulator is
	 * not started until {@link #start()} is called. By default, the stall time is set to {@link Double#POSITIVE_INFINITY}
	 *
	 * @param process
	 *            The process that is being controlled.
	 * @param controller
	 *            The controller that is being used to control the process.
	 * @param driver
	 *            The driver that is being used to produce to target values for
	 *            the process.
	 * @param monitor
	 *            The monitor that is being used to obtain feedback from the
	 *            process.
	 * @deprecated When a driver is specified on construction of the Regulator, it is impossible to provide the {@link TaskMonitor} associated with
	 *             that driver. To set the driver and receive the associated {@link TaskMonitor}, use the {@link #setDriver(Driver)} method after
	 *             using this constructor: {@link #Regulator(Controllable, Controller, Monitor)}.
	 *
	 */
	@Deprecated
	public Regulator(Controllable process, Controller controller, Driver driver, Monitor monitor) {
	
		this(process, controller, driver, monitor, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Makes a new regulator with the specified information. The regulator is
	 * not started until {@link #start()} is called.
	 *
	 * @param process
	 *            The process that is being controlled.
	 * @param controller
	 *            The controller that is being used to control the process.
	 * @param driver
	 *            The driver that is being used to produce to target values for
	 *            the process.
	 * @param monitor
	 *            The monitor that is being used to obtain feedback from the
	 *            process.
	 * @param stallTime
	 *            The minimum time for which the controller must remain stalled in order for the driver to be informed of a stall.
	 *
	 */
	private Regulator(Controllable process, Controller controller, Driver driver, Monitor monitor, double stallTime) {
	
		this.process = process;
		this.controller = controller;
		this.driver = driver;
		taskMonitor = new TaskMonitor();
		driverLock = new Object();
		this.monitor = monitor;
		this.stallTime = stallTime;
		
		// Set up the logger
		isLogging = false;
		log = new RegulationLog();
		
		// Make the regulator, make it a daemon thread and then start it.
		regulationActive = false;
		regulationLoop = new RegulationLoop();
		regulationLoop.setDaemon(true);
		regulationLoop.start();
		
		// DEBUG
		resetError();
	}
	
	/**
	 * Changes the driver.
	 *
	 * @param newDriver
	 *            The new driver.
	 * @return The {@link TaskMonitor} associated with the new driver.
	 */
	public TaskMonitor setDriver(Driver newDriver) {
	
		synchronized (driverLock) {
			
			// Abort the last task
			taskMonitor.setState(STATE.ABORTED);
			
			// Change the driver and make a new TaskMonitor.
			driver = newDriver;
			// controller.reset();
			taskMonitor = new TaskMonitor();
			return taskMonitor;
		}
	}
	
	/**
	 * Gets the current driver. This method should rarely be used; there is no guarantee as to the driver that will be returned. The most recent
	 * driver is always returned, which may not be what is wanted.
	 *
	 * @return The current driver.
	 */
	public Driver getCurrentDriver() {
	
		return driver;
	}
	
	/**
	 * Gets the current task monitor. This method should rarely be used; there is no guarantee as to the driver that the returned task monitor will be
	 * associated with. Instead, the client should store the result of {@link #setDriver(Driver)} and then, on the result, call
	 * {@link TaskMonitor#getState()}.
	 *
	 * @return The current {@link TaskMonitor}.
	 */
	public TaskMonitor getCurrentTaskMonitor() {
	
		return taskMonitor;
	}
	
	/**
	 * Stops regulation. Before the regulation is stopped, <code>process.halt()</code> is called to ensure that the regulator
	 * releases control. The state of the current move is also set to {@link STATE#SUSPENDED}.
	 */
	public void stop() {
	
		synchronized (regulationLoop) {
			regulationActive = false;
		}
		synchronized (driverLock) {
			taskMonitor.setState(STATE.SUSPENDED);
		}
	}
	
	/**
	 * Restarts regulation.
	 */
	public void start() {
	
		synchronized (regulationLoop) {
			regulationActive = true;
			regulationLoop.notifyAll();
		}
		synchronized (driverLock) {
			taskMonitor.setState(STATE.IN_PROGRESS);
		}
	}
	
	/**
	 * Returns true if regulation is active.
	 *
	 * @return True if regulation is active.
	 */
	public boolean isRunning() {
	
		synchronized (regulationLoop) {
			return regulationActive;
		}
	}
	
	/**
	 * @return the stallTime
	 */
	public double getStallTime() {
	
		return stallTime;
	}
	
	/**
	 * @param stallTime
	 *            the stallTime to set
	 */
	public void setStallTime(double stallTime) {
	
		this.stallTime = stallTime;
	}
	
	/**
	 * Sets the state of logging.
	 *
	 * @param shouldLog
	 *            True if the regulator should keep a log.
	 */
	public void setLoggingState(boolean shouldLog) {
	
		isLogging = shouldLog;
	}
	
	/**
	 * Gets the current log.
	 *
	 * @return The current log.
	 */
	public RegulationLog getLog() {
	
		return log;
	}
	
	/**
	 * Resets all the error statistics.
	 */
	public void resetError() {
	
		sumE2 = 0;
		numError = 0;
	}
	
	/**
	 * Gets the current Root Mean Square Error (RMSE). The RMSE is calculated by keeping a running total of the squares of errors from
	 * every regulation cycle. Then, it is divided by the number of cycles.
	 *
	 * @return The RMSE. A smaller number means that the motor is performing better.
	 */
	public double getRMSE() {
	
		return Math.sqrt(sumE2 / numError);
	}
	
	/**
	 * The thread that does the regulation.
	 *
	 * @author Jacob Glueck
	 */
	private class RegulationLoop extends Thread {
		
		@Override
		public void run() {
		
			while (true) {
				
				// Reset the controller.
				controller.reset();
				double stallStart = Double.POSITIVE_INFINITY;
				
				// Regulate while regulation is active
				while (regulationActive) {
					
					// Get the target and the current value
					double target;
					double currentValue = monitor.getCurrentValue();
					synchronized (driverLock) {
						
						// Get the target and the current time
						target = driver.getTarget();
						double currentTime = System.currentTimeMillis() / 1000;
						
						// Check to see if the controller is stalled and if there is no recorded stall starting time. If so, mark a stall start.
						if (controller.isStalled() && stallStart == Double.POSITIVE_INFINITY)
							stallStart = currentTime;
						
						// Update the status
						if (driver.moveComplete(currentValue))
							taskMonitor.setState(STATE.COMPLETED);
						else if (currentTime - stallStart > stallTime)
							taskMonitor.setState(STATE.STALLED);
						else
							// System.out.println(((BasicMotor) process).getID() + " complete.");
							taskMonitor.setState(STATE.IN_PROGRESS);
					}
					
					// Send the output
					// System.out.println("T: " + target + " C:" + currentValue);
					// if (driver.moveComplete(currentValue)) {
					// System.out.println("COMP");
					// Button.waitForAnyPress();
					// }
					double output = controller.getOutput(currentValue, target);
					// if (((BasicMotor) process).getID() == 'B')
					// System.out.println((int) output);
					// if (output == 0)
					// System.out.println(((BasicMotor) process).getID() + " ZERO.");
					// if (shouldSend)
					
					// EXPERIMENTAL: Smooth Output! Reduce output for small corrections, to reduce jitter
					// output *= 1 - 1 / Math.exp(Math.pow(output / 20, 2));
					
					process.control(output);
					if (isLogging)
						log.log(System.currentTimeMillis(), currentValue, target, output);
					
					// DEBUG
					sumE2 += Math.pow(currentValue - target, 2);
					numError++;
				}
				
				process.halt();
				
				// Wait until notified
				synchronized (this) {
					while (!regulationActive)
						try {
							wait();
						} catch (InterruptedException e) {
						}
				}
			}
		}
	}
}