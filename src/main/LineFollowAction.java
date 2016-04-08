package main;

/**
 * Represents a new line follow action.
 */
public class LineFollowAction extends RobotAction {

	/**
	 * The start node.
	 */
	private final String startNode;
	/**
	 * The end node.
	 */
	private final String endNode;
	
	/**
	 * Makes a new line follow action.
	 *
	 * @param startNode
	 *            the starting node.
	 * @param endNode
	 *            the ending node.
	 * @param seqNum
	 *            the sequence number.
	 */
	public LineFollowAction(String startNode, String endNode, int seqNum) {
	
		super(seqNum);
		this.startNode = startNode;
		this.endNode = endNode;
	}

	/**
	 *
	 */
	@Override
	protected String dataToString() {
	
		return startNode + "->" + endNode;
	}

	/**
	 * Gets the start node.
	 * 
	 * @return the start node.
	 */
	public String getStartNode() {
	
		return startNode;
	}

	/**
	 * Gets the end node.
	 * 
	 * @return the end node.
	 */
	public String getEndNode() {
	
		return endNode;
	}
	
}
