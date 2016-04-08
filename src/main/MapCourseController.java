package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import routefinder.Path;
import routefinder.ShortestPathCalculator;

/**
 * This class controls the robot's course through the city.
 *
 * @author tal
 *
 */

public class MapCourseController implements CourseController {

	public static String MAP_NAME = "testmap.txt";// "citymap.txt";

	private int seq = 0;
	private RobotAction nextAction = new PulloutAction(ION2016.LEFT, seq);

	private final ShortestPathCalculator robotPath;
	private final Path thePath;
	private final String[] nodeOrder;
	private int currentNode = 0;
	private int parkSide = -5;

	/**
	 * Makes a new course controlled that will cause the robot to visit the specified lots.
	 *
	 * @param lotNumbers
	 *            the lots that the robot should visit.
	 * @param startingBase
	 *            the starting base number. 1 is the old base, 2 is the new base.
	 */
	public MapCourseController(Set<Integer> lotNumbers, int startingBase) {

		String baseName;
		String endNode;
		if (startingBase == 1) {

			baseName = "L00A";
			endNode = "I00";
		} else {

			baseName = "L00B";
			endNode = "I08";
			
		}

		ArrayList<String> requiredLots = new ArrayList<String>();

		for (Integer i : lotNumbers)
			if (i < 10)
				requiredLots.add("L0" + i + "A");
			else
				requiredLots.add("L" + i + "A");

		robotPath = new ShortestPathCalculator(baseName, endNode, requiredLots, MAP_NAME);
		thePath = robotPath.cheapestPath;
		
		ArrayList<String> pathList = new ArrayList<String>();
		
		for (String s : thePath.getPath())
			pathList.add(s);
		
		nodeOrder = new String[pathList.size()];
		
		for (int i = 0; i < nodeOrder.length; i++)
			nodeOrder[i] = pathList.get(i);
	}

	/**
	 * Called to inform the course controlled of an event and allow the course controller to decide the next action.
	 *
	 * @param event
	 *            the event to inform the course controller of.
	 */
	@Override
	public void logEvent(RobotMessage event) {

		seq++;
		if (event.equals(RobotMessage.INTERSECTION_DETECTED)) {

			while (nodeOrder[currentNode].contains("L"))
				currentNode++;

			if (currentNode == nodeOrder.length - 1) {
				
				nextAction = new CelebrateAction(seq);
				return;
			}
			
			String nodeA = nodeOrder[currentNode - 1];
			String nodeB = nodeOrder[currentNode];
			String nodeC = nodeOrder[currentNode + 1];
			
			int angle = robotPath.calculateTurnAngle(nodeA, nodeB, nodeC);
			
			nextAction = new IntersectionAction(angle, seq);
			
			currentNode++;
			
		}
		if (event.equals(RobotMessage.INTERSECTION_NAVIGATED))
			nextAction = new LineFollowAction(nodeOrder[currentNode - 1], nodeOrder[currentNode], seq);

		if (event.equals(RobotMessage.PARKING_LOT_RIGHT_DETECTED))
			if (nodeOrder[currentNode].equals(nodeOrder[currentNode + 1])) {

				nextAction = new ParkAction(3, ION2016.RIGHT, seq);
				currentNode++;
				parkSide = ION2016.RIGHT;
			} else
				nextAction = new LineFollowAction(nodeOrder[currentNode], nodeOrder[currentNode + 1], seq);
		
		if (event.equals(RobotMessage.PARKING_LOT_LEFT_DETECTED))
			if (nodeOrder[currentNode].equals(nodeOrder[currentNode + 1])) {

				nextAction = new ParkAction(3, ION2016.LEFT, seq);
				currentNode++;
				parkSide = ION2016.LEFT;
			} else
				nextAction = new LineFollowAction(nodeOrder[currentNode], nodeOrder[currentNode + 1], seq);
		
		if (event.equals(RobotMessage.PARKED)) {

			if (parkSide == -5) {
				
				System.out.println("Robot never asked to park.");
				return;
			} else if (parkSide == ION2016.LEFT)
				nextAction = new PulloutAction(ION2016.LEFT, seq);
			else if (parkSide == ION2016.RIGHT)
				nextAction = new PulloutAction(ION2016.RIGHT, seq);
			
			parkSide = -5;
		}
		if (event.equals(RobotMessage.PULLED_OUT))
			nextAction = new LineFollowAction(nodeOrder[currentNode], nodeOrder[currentNode + 1], seq);

		// TODO Store the event, and use it to determine the robot's current position.
		// The event will be one of the following:
		// RobotMessage.INTERSECTION_DETECTED
		// RobotMessage.INTERSECTION_NAVIGATED
		// RobotMessage.PARKING_LOT_LEFT_DETECTED
		// RobotMessage.PARKING_LOT_RIGHT_DETECTED
		// RobotMessage.PARKED
		// RobotMessage.PULLED_OUT

	}

	/**
	 * Gets the next action.
	 *
	 * @return the action the robot should do next. If there are no more actions (the robot has finished), this method should return a
	 *         {@link CelebrateAction}.
	 */
	@Override
	public RobotAction getNextAction() {

		return nextAction;
		// TODO Use the current position to get the next action.
		// new LineFollowAction(startNode, endNode, seqNum)
		// new ParkAction(numSpots, side, seqNum)
		// new PulloutAction(side, seqNum)
		// new IntersectionAction(angle, seqNum)
		// new CelebrateAction(seqNum)
		// Sequence numbers should just be the move number. Essentially, it is for debugging; if something goes wrong with threading, these will be
		// helpful.
	}
	
	public static void main(String args[]) {

		HashSet<Integer> lots = new HashSet<Integer>();
		lots.add(1);
		lots.add(2);
		lots.add(5);
		lots.add(6);
		int startingBase = 2;
		
		MapCourseController test = new MapCourseController(lots, startingBase);
		
		RobotMessage msg[] = new RobotMessage[6];
		msg[0] = RobotMessage.INTERSECTION_DETECTED;
		msg[1] = RobotMessage.INTERSECTION_NAVIGATED;
		msg[2] = RobotMessage.PARKING_LOT_LEFT_DETECTED;
		msg[3] = RobotMessage.PARKING_LOT_RIGHT_DETECTED;
		msg[4] = RobotMessage.PARKED;
		msg[5] = RobotMessage.PULLED_OUT;
		
		/*
		 * 1. Intersection_Detected
		 * 2. Intersection_Navigated
		 * 3. Parking_Lot_Left_Detected
		 * 4. Parking_Lot_Right_Detected
		 * 5. Parked
		 * 6. PulledOut
		 * 7. Stop
		 */

		System.out.println(test.getNextAction());
		Scanner in = new Scanner(System.in);
		System.out.print("Enter next choice: ");
		int choice = in.nextInt();
		
		while (choice != 7) {
			
			test.logEvent(msg[choice - 1]);
			
			System.out.println(test.getNextAction());
			
			System.out.print("Enter next choice: ");
			choice = in.nextInt();
			
		}

	}
}
