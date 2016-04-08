package utilities.filter;

import java.util.NoSuchElementException;

/**
 * This is a mean value filter. It stores the last inputs and calculates the filtered output as an average of the recent previous inputs.
 * 
 * @author Jacob Glueck
 * 
 */
public class MeanValueFilter implements Filter {
	
	/**
	 * Stores the previous values used for computing the average.
	 */
	RingBuffer input;
	
	/**
	 * Makes a new mean value filter with the specified value for n. n represents the number of values the filter will use in the average when
	 * calculating the output. For example, if n = 5, the result of {@link #getFilteredOutput()} will be the average of the most recent 5 values
	 * passed to {@link #addValue(double)}.
	 * 
	 * @param n
	 *            The number of values to average.
	 */
	public MeanValueFilter(int n) {
	
		input = new RingBuffer(n);
	}
	
	/**
	 * Adds a value of the function to be filtered.
	 * 
	 * @param value
	 *            The value.
	 */
	@Override
	public void addValue(double value) {
	
		input.add(value);
		// System.out.println(input + "|||\t\t" + value);
	}
	
	/**
	 * Averages the last n values passed to the {@link #addValue(double)} method, where n is the number that was passed into the constructor.
	 */
	@Override
	public double getFilteredOutput() {
	
		if (input.size == 0)
			throw new IllegalStateException("There are no input values!");
		
		double total = 0;
		for (int x = 0; x < input.size; x++)
			total += input.get(x);
		
		return total / input.size;
	}
	
	/**
	 * This is a ring buffer.
	 * 
	 * @author Jacob Glueck
	 */
	private class RingBuffer {
		
		/**
		 * The index of the front element.
		 */
		private int frontLoc;
		/**
		 * The index where the next element will go.
		 */
		private int addLoc;
		/**
		 * The current number of elements.
		 */
		private int size;
		/**
		 * The data.
		 */
		private final double[] data;
		
		/**
		 * Makes a new ring buffer of length n.
		 * 
		 * @param n
		 *            The length of the ring buffer.
		 */
		public RingBuffer(int n) {
		
			frontLoc = 0;
			addLoc = 0;
			size = 0;
			data = new double[n];
		}
		
		/**
		 * Adds an element to the end of the queue
		 * 
		 * @param element
		 *            The element to add.
		 */
		public void add(double element) {
		
			// If the array needs to be resized
			if (size == data.length)
				remove();
			
			// Add the element
			data[addLoc] = element;
			addLoc = increment(addLoc);
			size++;
		}
		
		/**
		 * Removes the first element and returns it
		 * 
		 * @return The element that was removed
		 */
		public double remove() {
		
			double toReturn = peek();
			frontLoc = increment(frontLoc);
			size--;
			return toReturn;
		}
		
		/**
		 * Returns the first element but does not remove it.
		 * 
		 * @return The first element.
		 */
		public double peek() {
		
			if (size == 0)
				throw new NoSuchElementException();
			return data[frontLoc];
		}
		
		/**
		 * Helper method to get the value at an index relative to front loc.
		 * 
		 * @param index
		 *            The index.
		 * @return The value at the specified index.
		 */
		public double get(int index) {
		
			return data[(index + frontLoc) % data.length];
		}
		
		/**
		 * Moves a variable forwards by one and wraps it.
		 * 
		 * @param curVal
		 *            The current value.
		 * @return (curVal + 1) % data.length.
		 */
		private int increment(int curVal) {
		
			return (curVal + 1) % data.length;
		}
		
		/**
		 * Returns a string with the contents of the buffer.
		 */
		@Override
		public String toString() {
		
			String result = "";
			for (int x = 0; x < size; x++)
				result += Math.round(get(x) * 100) / 100.0 + " ";
			return result;
		}
	}
	
}
