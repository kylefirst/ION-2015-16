package routefinder;
import java.util.LinkedList;


public class Path {

	
	private LinkedList<String> path;
	private Double cost;
	
	public Path(LinkedList path,Double cost){
		
		this.path=path;
		this.cost=cost;
	}
	
	
	public LinkedList<String> getPath(){
		
		return path;
		
		
	}
	
	public Double getCost(){
		
		return cost;
	}
			
	public String toString(){
		String result = path.toString();
		result+="\n"+cost;
		return result;
	}
}
