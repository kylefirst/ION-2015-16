package routefinder;
import java.util.*;

/*Tal Davidi 
 *4/10/14
 *Pd.1 Programming 3
 * 
 *A data structure which represents a graph
 *
 *4/29/14
 *Added Bellman-Ford's algorithm functionality
 */
public class Graph {

	private Map<String,Map<String,Double>> data;
	private Map<String,Map<String,Integer>> startAngles;
	private Map<String,Map<String,Integer>> endAngles;

	//creates an empty graph
	public Graph(){

		data=new HashMap<String,Map<String,Double>>();
		startAngles = new HashMap<String,Map<String,Integer>>();
		endAngles = new HashMap<String,Map<String,Integer>>();
		
	}


	//prints out a table with djikstra's algorithm applied to find the shortest path to each node from the source
	public Path shortestLengthsDijkstra(String src,String destination){

		//creates empty sets for s,p, and c
		Set<String> s=new HashSet<String>();
		Map<String,String> previous=new HashMap<String,String>();
		Map<String,Double> costs=new HashMap<String,Double>();

		Set<String> keys=data.keySet();

		int totalNodes=0;
		int nodesChecked=0;

		//sets the previous for each node to null and the cost to infinity
		for(String node:keys){

			totalNodes++;
			costs.put(node, Double.POSITIVE_INFINITY);
			previous.put(node, null);
		}

		costs.put(src, 0.0);
		
		//runs until destination is in s
		while(!s.contains(destination)){

			String k=null;

	
			for(String node:keys){

				//for each node in the graph, finds the node k not in s with the lowest cost
				if(!s.contains(node)){

					if(k==null || costs.get(node)<costs.get(k)) {
						
						k=node;
					}
					
				}
			}

			s.add(k);
			nodesChecked++;

			//gets all the neighbors of k
			Map<String,Double> adjacent=data.get(k);
			if(adjacent==null){
				System.out.println(k+" Not Found!!!");
				System.exit(-1);
			}
			Set<String> dest=adjacent.keySet();

			for(String node:dest){

				if(!s.contains(node)){

					//updates the cost to get to that node if it is less
					Double newCost=costs.get(k)+adjacent.get(node);

					if(costs.get(node)>newCost){

						costs.put(node, newCost);
						previous.put(node, k);
					}
				}
			}
		}
		
		Double cost = costs.get(destination);
		
		LinkedList<String> path=new LinkedList<String>();
		
		String current=destination;
		String prevNode=previous.get(current);
		
		
		path.add(destination);
		
		while(prevNode!=null){

			current=prevNode;
			path.addFirst(current);
			
			prevNode=previous.get(current);
			
	
		}
		
		return new Path(path,cost);



	}

	//adds a node to the set with no connections
	public void addNode(String name){

		if(data.containsKey(name)){

			throw new IllegalArgumentException("Node already exists");
		}

		data.put(name,new HashMap<String,Double>());
		startAngles.put(name,new HashMap<String,Integer>());
		endAngles.put(name,new HashMap<String,Integer>());
	}

	//adds an arc from the first node to the second as long as no connection exists
	public void addArc(String src,String dest,double weight,int startAngle,int endAngle){

		//adds the arc from src to dest
		Map<String,Double> srcMap=data.get(src);
		srcMap.put(dest,weight);
		
		Map<String,Integer> startAng = startAngles.get(src);
		Map<String,Integer> endAng = endAngles.get(src);
		
		startAng.put(dest, startAngle);
		endAng.put(dest, endAngle);
	}
	
	public Integer getStartAngle(String src,String dest){
		
		Map<String,Integer> startAng = startAngles.get(src);
		
		return startAng.get(dest);
	}
	
	public Integer getEndAngle(String src,String dest){
		
		Map<String,Integer> endAng = endAngles.get(src);
		
		return endAng.get(dest);
	}

	//Adds an edge between two nodes in a graph with the weight specified
	public void addEdge(String src,String dest, double weight){

		if(!nodesExist(src,dest)){

			throw new IllegalArgumentException("One or two of the nodes does not exist");
		}

		if(isConnected(src,dest)){

			throw new IllegalArgumentException("A connection exists between the two nodes");
		}

		//adds a connection between both the nodes
		Map<String,Double> srcMap=data.get(src);
		Map<String,Double> destMap=data.get(dest);

		srcMap.put(dest,weight);
		destMap.put(src,weight);
	}
	


	//removes an arc from one node to another.
	//Throws if the arc does not exist
	public void removeArc(String src,String dest){

		if(!nodesExist(src,dest)){

			throw new IllegalArgumentException("One or two of the nodes does not exist");
		}

		Map<String,Double> srcMap=data.get(src);

		if(!srcMap.containsKey(dest)){

			throw new IllegalArgumentException("That arc does not exist");
		}

		srcMap.remove(dest);
	}

	//removes an arc from one node to another.
	//Throws if the arc does not exist
	public void removeEdge(String src,String dest){

		if(!nodesExist(src,dest)){

			throw new IllegalArgumentException("One or two of the nodes does not exist");
		}


		Map<String,Double> srcMap=data.get(src);
		Map<String,Double> destMap=data.get(dest);

		if(!(srcMap.containsKey(dest) && destMap.containsKey(src))){

			throw new IllegalArgumentException("That edge does not exist");
		}

		//removes the edge between both nodes
		srcMap.remove(dest);
		destMap.remove(src);
	}

	//returns true if a connection exists from one node to the other
	private boolean isConnected(String src,String dest){

		Map<String,Double> srcMap=data.get(src);
		Map<String,Double> destMap=data.get(dest);

		if(srcMap.containsKey(dest) || destMap.containsKey(src)){

			return true;
		}

		return false;
	}

	//returns true if both nodes exist
	private boolean nodesExist(String first,String second){

		return data.containsKey(first) && data.containsKey(second);
	}
	
	public String toString(){
		
		return data.toString();
	}
}
