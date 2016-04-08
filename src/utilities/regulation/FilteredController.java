package utilities.regulation;

import utilities.filter.Filter;

/**
 * @author jacob
 * 
 */
public class FilteredController implements Controller {
	
	/**
	 * The controller to filter.
	 */
	private final Controller c;
	/**
	 * The filter to use.
	 */
	private final Filter f;
	
	/**
	 * Makes a new filtered controller with the specified information. A filtered controller relies on the underlying controller for output and then
	 * passes the output through the filter.
	 * 
	 * @param c
	 *            The controller.
	 * @param f
	 *            The filter.
	 */
	public FilteredController(Controller c, Filter f) {
	
		this.c = c;
		this.f = f;
	}
	
	/**
	 * Uses the underlying controller to get the output and the passes it through the filter.
	 */
	@Override
	public double getOutput(double currentValue, double targetValue) {
	
		f.addValue(c.getOutput(currentValue, targetValue));
		return f.getFilteredOutput();
	}
	
	@Override
	public void reset() {
	
		c.reset();
	}
	
	@Override
	public boolean isStalled() {
	
		return c.isStalled();
	}
	
}
