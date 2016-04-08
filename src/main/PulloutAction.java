package main;

/**
 * Represents a pull out action.
 */
public class PulloutAction extends RobotAction {
	
	/**
	 * The side on which to pull out. Should be either {@link ION2016#LEFT} or {@link ION2016#RIGHT}.
	 */
	private final int side;

	/**
	 * Makes a new pull out action with the specified information.
	 *
	 * @param side
	 *            the side on which to pull out. Should be either {@link ION2016#LEFT} or {@link ION2016#RIGHT}.
	 * @param seqNum
	 *            the sequence number
	 */
	public PulloutAction(int side, int seqNum) {
	
		super(seqNum);
		this.side = side;
	}
	
	/**
	 *
	 */
	@Override
	protected String dataToString() {

		String r = side == ION2016.LEFT ? "left" : "right";
		return "side=" + r;
	}
	
	/**
	 * Gets the side.
	 * 
	 * @return the side, either {@link ION2016#LEFT} or {@link ION2016#RIGHT}.
	 */
	public int getSide() {
	
		return side;
	}
	
}
