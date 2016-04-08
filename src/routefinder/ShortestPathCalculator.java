package routefinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class ShortestPathCalculator {
	
	private final Graph map;
	private final ArrayList<String> requiredLots;
	private final String startNode; // first node in the list
	private final String endNode;
	
	// keys are in form src:dest
	// the value is a linked list representing the shortest path from src to
	// dest and the cost
	private final HashMap<String, Path> shortestPaths = new HashMap<String, Path>(); // src
	
	private ArrayList<LinkedList<String>> lotOrders = new ArrayList<LinkedList<String>>();
	
	public Path cheapestPath;
	
	public ShortestPathCalculator(String startNode, String endNode, ArrayList<String> requiredLots, String mapName) {
	
		map = new Graph();
		loadMap(mapName);
		
		// System.out.println(map.toString());
		
		this.requiredLots = requiredLots;
		this.startNode = startNode;
		this.endNode = endNode;

		// computes shortest path between each lot
		permuteDijkstras();
		
		// generate all possible orders of visiting the lots
		lotOrders = calculateLotOrders();
		
		cheapestPath = calculateCheapestPath();
		
		System.out.println(cheapestPath);
	}
	
	// loads each node with the lengths between them
	private void loadMap(String fName) {
	
		File f = new File(fName);
		Scanner inFile = null;
		
		try {
			
			inFile = new Scanner(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		// reads in the nodes and lengths
		while (inFile.hasNextLine()) {
			
			String arc = inFile.nextLine();
			
			// parses the src, dest, and length
			String src = arc.substring(0, arc.indexOf("-"));
			String dest = arc.substring(arc.indexOf("-") + 1, arc.indexOf(":"));
			Double len = Double.parseDouble(arc.substring(arc.indexOf(":") + 1, arc.indexOf(";")));

			String angleStr = arc.substring(arc.indexOf(";") + 1, arc.length());
			int angle1;
			int angle2;

			if (angleStr.contains("-")) {

				angle1 = Integer.parseInt(angleStr.substring(0, angleStr.indexOf("-")));
				angle2 = Integer.parseInt(angleStr.substring(angleStr.indexOf("-") + 1, angleStr.length()));
			} else {

				angle1 = Integer.parseInt(angleStr);
				angle2 = angle1;
			}
			
			try {
				
				map.addNode(src);
				
			} catch (IllegalArgumentException e) {
				// do nothing
			}
			
			try {
				
				map.addNode(dest);
			} catch (IllegalArgumentException e) {
				
				// do nothing
			}
			
			try {
				
				map.addArc(src, dest, len, angle1, angle2);
				
			} catch (IllegalArgumentException e) {
				
				// do nothing
			}
		}
	}
	
	// calculates shortest paths from start node to every element of required
	// lots
	// calculates shortest paths between every element of required lots
	private void permuteDijkstras() {
	
		// shortest path between start node and every node
		for (String dest : requiredLots) {
			
			shortestPaths.put(startNode + ":" + dest, map.shortestLengthsDijkstra(startNode, dest));

			shortestPaths.put(dest + ":" + endNode, map.shortestLengthsDijkstra(dest, startNode));
		}
		
		// shortest path between every lot
		for (String src : requiredLots)
			for (String dest : requiredLots)
				if (!src.equals(dest))
					shortestPaths.put(src + ":" + dest, map.shortestLengthsDijkstra(src, dest));
	}
	
	// calculates all the possible paths between lots starting with startNode
	private ArrayList<LinkedList<String>> calculateLotOrders() {
	
		LinkedList<String> pathSoFar = new LinkedList<String>();
		pathSoFar.addFirst(startNode);
		
		ArrayList<String> lotsLeft = new ArrayList<String>(requiredLots);
		
		return recLotHelper(lotsLeft, pathSoFar);
		
	}
	
	// helper method to calculate possible paths
	private ArrayList<LinkedList<String>> recLotHelper(ArrayList<String> lotsLeft, LinkedList<String> pathSoFar) {
	
		ArrayList<LinkedList<String>> toReturn = new ArrayList<LinkedList<String>>();
		
		// only one lot left to add
		if (lotsLeft.size() == 1) {
			
			pathSoFar.add(lotsLeft.get(0));
			pathSoFar.add(endNode);
			toReturn.add(pathSoFar);
			return toReturn;
		}
		
		// recursively calculates all possible paths and takes the union
		
		for (String next : lotsLeft) {
			
			LinkedList<String> possiblePath = new LinkedList<String>(pathSoFar);
			possiblePath.add(next);
			
			ArrayList<String> lotsNowLeft = new ArrayList<String>(lotsLeft);
			lotsNowLeft.remove(next);
			
			ArrayList<LinkedList<String>> pathSubset = recLotHelper(lotsNowLeft, possiblePath);
			
			toReturn.addAll(pathSubset);
			
		}
		
		return toReturn;
		
	}
	
	// returns the shortest path possible
	private Path calculateCheapestPath() {
	
		Path cheapest = null;
		
		for (LinkedList<String> lotOrder : lotOrders) {
			
			double cost = 0;
			LinkedList<String> nodeOrder = new LinkedList<String>();
			
			Iterator<String> iter = lotOrder.listIterator();
			
			String current = iter.next();
			String next;
			
			while (iter.hasNext()) {
				
				next = iter.next();
				
				Path shortestPath = shortestPaths.get(current + ":" + next);
				
				cost += shortestPath.getCost();
				
				nodeOrder.addAll(shortestPath.getPath());
				
				current = next;
				
			}
			
			nodeOrder.add(endNode);
			Path toCompare = new Path(nodeOrder, cost);
			
			if (cheapest == null)
				cheapest = toCompare;
			else if (toCompare.getCost() < cheapest.getCost())
				cheapest = toCompare;
			
		}
		
		return cheapest;
		
	}
	
	// returns an angle from -90 to 90 which indicates the angle the robot should turn, positive is right, neg is left
	public int calculateTurnAngle(String nodeA, String nodeB, String nodeC) throws IllegalArgumentException {
	
		int angle1 = map.getEndAngle(nodeA, nodeB); // robots start angle
		int angle2 = map.getStartAngle(nodeB, nodeC); // robots end angle
		
		int turnAngle = angle1 - angle2;
		
		int toRet;
		if (turnAngle >= 270 && turnAngle <= 450)
			toRet = turnAngle - 360;
		else if (turnAngle >= -450 && turnAngle <= -270)
			toRet = turnAngle + 360;
		else if (turnAngle >= -90 && turnAngle <= 90)
			toRet = turnAngle;
		else {

			System.out.println("Turn angle is " + turnAngle + "with nodes:" + nodeA + ", " + nodeB + ", " + nodeC);
			System.out.println("Exiting Program");
			throw new IllegalArgumentException("Robot can not turn");
		}

		// System.out.println("Turn angle is " +toRet);
		return toRet;

	}

}
