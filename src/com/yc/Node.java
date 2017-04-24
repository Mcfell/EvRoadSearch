package com.yc; /***
 * 
 * @author selemon
 * 
 * */

import java.awt.*;
import java.util.*;
import java.util.List;


/** Node   */

public class Node implements Iterable<Road>{

    private int id;
    private Location loc;  // coordinates of the intersection
    private List<Segment> outNeighbours = new ArrayList<Segment>(2);
    private List<Segment> inNeighbours = new ArrayList<Segment>(2);
	private Node pathTo;
	private boolean visited = false;
	private double pathLength = -1;
	private int depth;
	private boolean articulationPoint;

	private static int wd = 14;
    private static int rad = wd/2;
    private static int dsq = rad*rad;

    /** Construct a new Node object */
    public Node(int id, Location l){
	this.id = id;
	loc = l;
    }





    /** Construct a new Node object from a line in the data file*/
    public Node(String line){
		String[] values = line.split("\t");
		id = Integer.parseInt(values[0]);
		double lat = Double.parseDouble(values[1]);
		double lon = Double.parseDouble(values[2]);
		loc = Location.newFromLatLon(lat, lon);
	//System.out.printf("Created Node %6d %s%n", id, loc);
    }

    public int getID(){
	return id;
    }
    public Location getLoc(){
	return this.loc;
    }

    public void addInSegment(Segment seg){
	inNeighbours.add(seg);
    }
    public void addOutSegment(Segment seg){
	outNeighbours.add(seg);
    }

    public List<Segment> getOutNeighbours(){
	return outNeighbours;
    }

    public List<Segment> getInNeighbours(){
	return inNeighbours;
    }

    public boolean closeTo(Location place, double dist){
	return loc.closeTo(place, dist);
    }

    public double distanceTo(Location place, int type){
	return loc.distanceTo(place,type);
    }

    public void draw(Graphics g, Location origin, double scale){
	Point p = loc.getPoint(origin, scale);
	g.fillRect(p.x, p.y, 2, 2);
    }



    public String toString(){
	StringBuilder b = new StringBuilder(String.format("Intersection %d: at %s; Roads:  ", id, loc));
	Set<String> roadNames = new HashSet<String>();
	for (Segment neigh : inNeighbours){
	    roadNames.add(neigh.getRoad().getName());
	}
	for (Segment neigh : outNeighbours){
	    roadNames.add(neigh.getRoad().getName());
	}
	for (String name : roadNames){
	    b.append(name).append(", ");
	}
	return b.toString();
    }




	public void visit() {
		visited=true;
	}

	public void setPathTo(Node n) {
		pathTo = n;
	}


	public boolean containsRoad(Road e){
		return (outNeighbours.contains(e) || inNeighbours.contains(e));
	    }
	public void setPathLength(double v){pathLength=v;}
	public double getPathLength(){return pathLength;}

	public Node getPathTo(){return pathTo;}
	public Node getPathTo(Node node) {
		return pathTo.getPathTo(node);
	}

	@Override
	public Iterator<Road> iterator() {
//		return (new Iterator <Road>(){private int nextIndx =0;
//	    public boolean hasNext(){return nextIndx<outNeighbours.size();}
//	    public Road next(){return edges.get(nextIndx++);}
//	    public void remove(){throw new UnsupportedOperationException();}});
		return null;
    }


	public boolean visited() {
		return visited;
	}

	public List<Node> getNighbours(Node nd){
		List <Node> n = new ArrayList<Node>();
		for (Segment e: nd.getOutNeighbours()){
			n.add(e.getEndNode());
		}
		return n;
	}



	public void setDepth(int d){depth = d;}

	public void resetART() {

		 depth = Integer.MAX_VALUE;
		 articulationPoint = false;


	 }



	public int getDepth() {
		// TODO Auto-generated method stub
		return depth;
	}





	public void setArticulationPoint(boolean b) {
		// TODO Auto-generated method stub
		articulationPoint=true;
	}



}
