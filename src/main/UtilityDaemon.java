package main;

/**
 * @author jacob
 *
 */
public class UtilityDaemon {

	/**
	 * True if the sensor daemon is active, i.e. not terminated.
	 */
	private volatile boolean isActive;
	/**
	 * True if the sensor daemon is currently running.
	 */
	private volatile boolean isRunning;
	/**
	 * An object used to wait and notify to stop and start the daemon.
	 */
	private final Object runLock;
	/**
	 * The thread on which the daemon runs.
	 */
	private final Thread daemonThread;
	/**
	 * The stuff the utility daemon does while running
	 */
	private final Runnable task;
	
	/**
	 * Makes a new UtilityDaemon to run a background task given by task
	 *
	 * @param task
	 *            the task to run
	 */
	public UtilityDaemon(Runnable task) {

		this.task = task;

		// Set up and start the thread, which will, as isRunning is false, immediately go to sleep.
		isActive = true;
		isRunning = false;
		runLock = new Object();
		daemonThread = new Thread(new Daemon(), task.getClass().getSimpleName() + " daemon");
		daemonThread.setDaemon(true);
		daemonThread.start();
	}
	
	/**
	 * Starts the daemon thread. Ensures that the task has run at least once by the time this method returns.
	 */
	public void startDaemon() {

		if (!isActive)
			throw new IllegalStateException(task.getClass().getSimpleName() + " daemon has been terminated.");
		synchronized (runLock) {
			task.run();
			isRunning = true;
			runLock.notifyAll();
		}
	}
	
	/**
	 * Pauses the daemon thread. The thread can be restarted with {@link #startDaemon()}.
	 */
	public void pauseDeamon() {

		if (!isActive)
			throw new IllegalStateException(task.getClass().getSimpleName() + " daemon has been terminated.");
		// synchronized (runLock) {
		isRunning = false;
		// runLock.notifyAll();
		// }
	}

	/**
	 * Terminates the daemon thread. This is irreversible; the thread cannot be restarted. Compare to {@link #pauseDeamon()}.
	 */
	public void terminateDeamon() {

		if (!isActive)
			throw new IllegalStateException(task.getClass().getSimpleName() + " daemon has been terminated.");
		isActive = false;
		pauseDeamon();
		startDaemon();
		try {
			daemonThread.join();
		} catch (InterruptedException e) {
		}
		isRunning = false;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
	
		return isActive;
	}

	/**
	 * True if the daemon is currently running
	 *
	 * @return the isRunning
	 */
	public boolean isRunning() {
	
		return isRunning;
	}

	/**
	 * The runnable daemon.
	 *
	 * @author jacob
	 */
	private class Daemon implements Runnable {
		
		@Override
		public void run() {
		
			while (isActive) {
				while (isRunning)
					task.run();
				synchronized (runLock) {
					while (!isRunning)
						try {
							runLock.wait();
						} catch (InterruptedException e) {
						}
				}
			}
		}
	}
}