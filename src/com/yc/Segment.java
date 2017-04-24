package com.yc; /***
 * 
 * @author selemon
 * 
 * */

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Segment   */

public class Segment{

    private Road road;      // the road this segment is part of
    private double length;   // length of segment
    private Node startNode; // the intersection it starts at
    private Node endNode;   // the intersection it ends at
    private List<Location> coords = new ArrayList<Location>();  // coords for drawing


    /** Construct a new Segment object */
    public Segment(Road r, double l, Node start, Node end){
	road = r;
	length = l;
	startNode = start;
	endNode = end;
    }

    /** Construct a new Segment object from a line in the data file */
    public Segment(int start,int end, Map<Integer, Node> nodes){

		length = 10d;
		startNode = nodes.get(start);
		endNode = nodes.get(end);

		coords.add(startNode.getLoc());
		coords.add(endNode.getLoc());
    }
	public Segment(int roadid,int start,int end, Map<Integer, Road> roads, Map<Integer, Node> nodes){
		length = 0.0016d + Math.random() * 0.0004d;
		startNode = nodes.get(start);
		endNode = nodes.get(end);
		road = roads.get(roadid);
		coords.add(startNode.getLoc());
		coords.add(endNode.getLoc());
	}


	public Road getRoad(){
	return road;
    }
    public double getLength(){
	return length;
    }
    public Node getStartNode(){
	return startNode;
    }
    public Node getEndNode(){
	return endNode;
    }

    public void addCoord(Location loc){
	coords.add(loc);
    }
    public List<Location> getCoords(){
	return coords;
    }

    public Segment reverse(){
	Segment ans =  new Segment(road, length, endNode, startNode);
	ans.coords = this.coords;
	return ans;
    }


    /** draw the roadsegment on the graphics.
      For each location, shift the origin to origin and scale by scale*/
    public void draw(Graphics g, Location origin, double scale){
	if (!coords.isEmpty()){
	    //System.out.printf("Drawing road %d between nodes %d and %d%n", roadID, startNodeID, endNodeID);
	    Point p1 = coords.get(0).getPoint(origin, scale);
	    for (int i=1; i<coords.size(); i++){
			Point p2 = coords.get(i).getPoint(origin, scale);
			//System.out.printf("(%d,%d) to (%d,%d)%n",p1.x, p1.y, p2.x, p2.y);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1=p2;
	    }
	}
    }

    public String toString() {
		return String.format("%d: %4.2fkm from %d to %d", road.getID(), length, startNode.getID(), endNode.getID());
	}

}
