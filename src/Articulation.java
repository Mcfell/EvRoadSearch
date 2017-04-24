/***
 * 
 * @author selemon
 * 
 * */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Articulation {
    	// Fields
    	private List <Node> nodes = new ArrayList<Node>();
    	private List <Node> artPts = new ArrayList<Node>();
    	private List <Node> Neighbour = new ArrayList<Node>();
    	private int infinity =Integer.MAX_VALUE;
    	private int NumSubTree;


    	/** Construct a new Articulation object and set up the GUI
    	 */
    	public Articulation(Map<Integer,Node> node){

    		for(Entry<Integer, Node> n: node.entrySet()){
    			this.nodes.add(n.getValue());
    		}



    	}



    	// to get a nighbour of node
    	public List<Node> getNighbours(Node nd){
    		List <Node> n = new ArrayList<Node>();
    		for (Segment seg: nd.getOutNeighbours()){
    			n.add(seg.getEndNode());
    			//	n.add(e.getTo());
    		}
    		return n;
    	}

    	public void findArtPoints(Node start){
    		for(Node n:nodes){
    			n.resetART();
    		}
    		start.setDepth(0);
    		NumSubTree =0;
    		Neighbour=getNighbours(start);
    		for(Node nighbour :Neighbour){
    			if(nighbour.getDepth()==infinity){
    				recArtPts(nighbour,1,start);
    			}
    			if (NumSubTree>1){
    				artPts.add(start);
    				start.setArticulationPoint(true);
    			}
    		}
    	}


    	/**
    	 * @param artPts the artPts to set
    	 */
    	public void setArtPts(List <Node> artPts) {
    		this.artPts = artPts;
    	}


    	/**
    	 * @return the artPts
    	 */
    	public List <Node> getArtPts() {
    		return artPts;
    	}

    	public int recArtPts(Node nod, int d, Node fromNode) {
    		// TODO Auto-generated method stub
    		//recursive method
    		nod.setDepth(d);
    		int reachBack= d;
    		Neighbour=getNighbours(nod);
    		for(Node neighbour :Neighbour){
    			if(neighbour.getDepth()<infinity){
    				reachBack= Math.min(neighbour.getDepth(),reachBack);
    			}
    			else{
    				int childReach= recArtPts(neighbour, d+1,nod);
    				reachBack=Math.min(childReach, reachBack);
    				if(childReach>=d){
    					artPts.add(nod);
    					nod.setArticulationPoint(true);

    				}

    			}
    		}
    		return reachBack;
    	}





    }

