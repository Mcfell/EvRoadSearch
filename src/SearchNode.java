/***
 * 
 * @author selemon
 * 
 * */




/** SearchNode   */

public class SearchNode implements Comparable <SearchNode>{
    public Node node;
    public SearchNode from;
    public double costSoFar;
    public double estRemCost;
    public boolean visited;

//    private int cadar;


    /** Construct a new SearchNode object */
    public SearchNode(Node n, SearchNode f, double c, double h){
	node = n;
	from = f;
	costSoFar = c;
	estRemCost = h;
    }

	public SearchNode(Node n, SearchNode f, double c){
		node = n;
		from = f;
		costSoFar = c;
	}

    public int compareTo(SearchNode other){
    	double cost1 = costSoFar + estRemCost;
    	double cost2 = other.costSoFar + other.estRemCost;
    	if(cost1 < cost2)
    		return -1;
    	else if(cost1 > cost2)
    		return 1;
    	else
    		return 0;
    }

}
