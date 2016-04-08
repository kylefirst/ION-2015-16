package utilities.regulation;

import java.util.ArrayList;

/**
 * This class allows for regulation to be logged for analysis.
 * 
 * @author Jacob Glueck
 */
public class RegulationLog {
	
	/**
	 * Represents the headers of the columns in the log.
	 */
	public static final String[] HEADERS = { "TIME (s)", "ACTUAL", "TARGET", "OUTPUT" };
	
	/**
	 * The log itself. Each row contains the time, the actual value and the target value, as described in the header array. There is no guarantee that
	 * the log will be sorted in ascending order by time.
	 */
	private final ArrayList<Double[]> log;
	
	/**
	 * Makes a new logger.
	 */
	public RegulationLog() {
	
		log = new ArrayList<>();
	}
	
	/**
	 * Logs the specified data.
	 * 
	 * @param time
	 *            The time for the log in ms based on the system clock.
	 * @param actual
	 *            The actual process value.
	 * @param target
	 *            The target process value.
	 * @param output
	 *            The current output.
	 */
	public void log(long time, double actual, double target, double output) {
	
		log.add(new Double[] { time / 1000.0, actual, target, output });
	}
	
	@Override
	public String toString() {
	
		String result = arrayFormat(HEADERS) + "\n";
		
		System.out.println(result);
		for (Double[] data : log)
			// result += arrayFormat(data) + "\n";
			System.out.println(arrayFormat(data));
		return "";
	}
	
	/**
	 * Formats an array with tabs between each element.
	 * 
	 * @param a
	 *            The array to format.
	 * @return The formated array.
	 */
	private String arrayFormat(Object[] a) {
	
		String result = "";
		for (Object element : a)
			result += element + "\t";
		return result;
	}
}
