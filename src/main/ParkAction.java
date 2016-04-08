package main;

/**
 * Represents a park action.
 */
public class ParkAction extends RobotAction {
	
	/**
	 * The number of spots in the parking lot.
	 */
	private final int numSpots;
	/**
	 * The side on which to park. Should be either {@link ION2016#LEFT} or {@link ION2016#RIGHT}.
	 */
	private final int side;

	/**
	 * Makes a new park action.
	 *
	 * @param numSpots
	 *            the number of spots in the parking lot.
	 * @param side
	 *            the side on which to park. Should be either {@link ION2016#LEFT} or {@link ION2016#RIGHT}.
	 * @param seqNum
	 *            the sequence number.
	 */
	public ParkAction(int numSpots, int side, int seqNum) {
	
		super(seqNum);
		this.numSpots = numSpots;
		this.side = side;
	}
	
	/**
	 *
	 */
	@Override
	protected String dataToString() {

		String r = side == ION2016.LEFT ? "left" : "right";
		return "numSpots=" + numSpots + ", side=" + r;
	}

	/**
	 * Gets the number of spots in the parking lot.
	 *
	 * @return the number of spots in the parking lot.
	 */
	public int getNumSpots() {
	
		return numSpots;
	}

	/**
	 * Gets the side on which to park.
	 *
	 * @return the side, either {@link ION2016#LEFT} or {@link ION2016#RIGHT}.
	 */
	public int getSide() {
	
		return side;
	}
	
}
