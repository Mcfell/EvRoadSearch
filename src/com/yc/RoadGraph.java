package com.yc; /***
 * 
 * @author selemon
 * 
 * */

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

/** RoadMap: The list of the roads and the graph of the road network    */

public class RoadGraph{

    double westBoundary = Double.POSITIVE_INFINITY;
    double eastBoundary = Double.NEGATIVE_INFINITY;
    double southBoundary = Double.POSITIVE_INFINITY;
    double northBoundary = Double.NEGATIVE_INFINITY;
    private JTextArea textArea = new JTextArea(50,20);
    private JTextArea messageArea = new JTextArea(1, 80);
    //the map containing the graph of nodes (and roadsegments), indexed by the nodeID
    Map<Integer, Node> nodes = new HashMap<Integer, Node>();

    //the map of roads, indexed by the roadID
    Map<Integer, Road> roads = new HashMap<Integer, Road>();;

    //the map of roads, indexed by name
    Map<String,Set<Road>> roadsByName = new HashMap<String,Set<Road>>();;

    Set<String> roadNames = new HashSet<String>();

    private List<Segment>  searchPath;

    /** Construct a new RoadMap object */
    public RoadGraph(){

    }

    public String loadData(int num){
	// Read roads into roads array.
	// Read the nodes into the roadGraph array.
	// Read each road segment
	//   put the segment into the neighbours of the startNode
	//   If the road of the segment is not one way,
	//   also construct the reversed segment and put it into
	//   the neighbours of the endNode
	// Work out the boundaries of the region.

	String report= "";
	loadRoads(num);
	System.out.println("Creating intersections...");
	loadNodes(num);
	report += String.format("Created %,d intersections%n", nodes.entrySet().size());
	System.out.println("Creating road segments...");
	loadSegments(num);
	report += String.format("Created %,d road segments%n", numSegments());
	return report;
    }


    public void loadRoads(int num){
		//暂时不生成
		for (int i = 0; i < num; i++) {
			for (int j = 1; j <= num; j++) {
				int k = i * num + j;
				if (j < num) {
					Road road1 = new Road(k, k + "-" + (k + 1), "test", 3, 3);
					roadNames.add(k+"-"+(k+1));
					roads.put(k,road1);
				}
				if (i < num - 1) {
					Road road2 = new Road(k + num * num, k + "-" + (k + num), "test", 3, 3);
					roadNames.add(k+"-"+(k+num));
					roads.put(k+ num*num,road2);
				}


			}
		}
    }
	
    public void loadNodes(int num){
    	int k = 1;
		for (int i = 0; i < num; i++) {
			for (int j = 1; j <= num; j++) {

				Location location = new Location(Location.centerLat + i * 0.001700,Location.centerLon + j * 0.001700);
				Node node = new Node(k,location);
				nodes.put(k,node);
				k++;
			}
		}
    }

    public void loadSegments(int num){
		for (int i = 0; i < num; i++) {
			for (int j = 1; j <= num; j++) {
				int k = i*num + j;
				Segment seg;
				Node node1;Node node2;Segment revSeg;
				if (j < num) {
					seg = new Segment(k,k, k + 1,roads, nodes);
					node1 = seg.getStartNode();
					node2 = seg.getEndNode();
					node1.addOutSegment(seg);
					node2.addInSegment(seg);
					Road road = seg.getRoad();
					road.addSegment(seg);
					revSeg = seg.reverse();
					node2.addOutSegment(revSeg);
					node1.addInSegment(revSeg);
				}
				if ( i < num - 1 ) {
					seg = new Segment(k+ num*num,k,k+num,roads,nodes);
					node1 = seg.getStartNode();
					node2 = seg.getEndNode();
					node1.addOutSegment(seg);
					node2.addInSegment(seg);
					revSeg = seg.reverse();
					node2.addOutSegment(revSeg);
					node1.addInSegment(revSeg);
				}
			}
		}
    }


    public double[] getBoundaries(){
	double west = Double.POSITIVE_INFINITY;
	double east = Double.NEGATIVE_INFINITY;
	double south = Double.POSITIVE_INFINITY;
	double north = Double.NEGATIVE_INFINITY;

	for (Node node : nodes.values()){
	    Location loc = node.getLoc();
	    if (loc.x < west) {west = loc.x;}
	    if (loc.x > east) {east = loc.x;}
	    if (loc.y < south) {south = loc.y;}
	    if (loc.y > north) {north = loc.y;}
	}
	return new double[]{west, east, south, north};
    }

    public void checkNodes(){
	for (Node node : nodes.values()){
	    if (node.getOutNeighbours().isEmpty()&& node.getInNeighbours().isEmpty()){
		System.out.println("Orphan: "+node);
	    }
	}
    }

    public Map<Integer, Node> getNodes() {
		return nodes;
	}

    public int numSegments(){
	int ans = 0;
	for (Node node : nodes.values()){
	    ans += node.getOutNeighbours().size();
	}
	return ans;
    }



    public void redraw(Graphics g, Location origin, double scale){
	//System.out.printf("Drawing road graph. at (%.2f, %.2f) @ %.3f%n", origX, origY, scale);
	g.setColor(Color.black);
	for (Node node : nodes.values()){
	    for (Segment seg : node.getOutNeighbours()){

			seg.draw(g, origin, scale);
	    }
	}
	g.setColor(Color.blue);
	for(Node node : nodes.values()){
	    node.draw(g, origin, scale);
	}
    }


    private double mouseThreshold = 5;  //how close does the mouse have to be?
	private Node startNode;
	private ArrayList<Node> path;


    public Node findNode(Point point, Location origin, double scale){
	Location mousePlace = Location.newFromPoint(point, origin, scale);
	/* System.out.printf("find at %d %d -> %.3f %.3f -> %d %d %n",
	   point.x, point.y, x, y,
	   (int)((x-origX)*scale),(int)((y-origY)*(-scale)) );
	*/
	Node closestNode = null;
	double mindist = Double.POSITIVE_INFINITY;
	for (Node node : nodes.values()){
	    double dist = node.distanceTo(mousePlace, Location.LINE);
	    if (dist<mindist){
		mindist = dist;
		closestNode = node;
	    }
	}
	return closestNode;
    }

    /** Returns a set of full road names that match the query.
     *  If the query matches a full road name exactly, then it returns just that name*/
    public Set<String> lookupName(String query){
	Set<String> ans = new HashSet<String>(10);
	if (query==null) return null;
	query = query.toLowerCase();
	for (String name : roadNames){
	    if (name.equals(query)){  // this is the right answer
		ans.clear();
		ans.add(name);
		return ans;
	    }
	    if (name.startsWith(query)){ // it is an option
		ans.add(name);
	    }
	}
	return ans;
    }

    /** Get the Road objects associated with a (full) road name */
    public Set<Road> getRoadsByName(String fullname){
	return roadsByName.get(fullname);
    }

    /** Return a list of all the segments belonging to the road with the
     given (full) name. */
    public List<Segment> getRoadSegments(String fullname){
	Set<Road> rds = roadsByName.get(fullname);
	if (rds==null) { return null; }
	System.out.println("Found "+rds.size()+" road objects: "+rds.iterator().next());
	List<Segment> ans = new ArrayList<Segment>();
	for (Road road : rds){
	    ans.addAll(road.getSegments());
	}
	return ans;
    }

	public List<Segment> searchAStar(Node start, Node goal, List<Node> points) {
		if (start==null || goal == null) return null;
		System.out.println("the start Node is "+start.toString() + " the goal Node is "+goal.toString());
		long starttime = System.currentTimeMillis();
		searchPath = new ArrayList<Segment>();
		PriorityQueue<SearchNode> fringe = new PriorityQueue<SearchNode>();
		SearchNode startsn = new SearchNode(start, null, 0, start.distanceTo(goal.getLoc(), Location.LINE));
		fringe.add(startsn);
		System.out.println("please wait loading......");
		path = new ArrayList<Node>();
		HashMap<Node, SearchNode> nodeMap = new HashMap<Node, SearchNode>();
		nodeMap.put(start,startsn);
		SearchNode endsn = null;
		while (! fringe.isEmpty()){
			SearchNode sn = fringe.poll();
			Node node = sn.node;
			if (node==goal) {
				endsn = sn;
				//System.out.println("The node your looking for is " + node.toString());
				//System.out.println("costSoFar is :" + sn.costSoFar);
				break;
			}
			if (sn.visited) continue;
			sn.visited = true;
			//System.out.println("----estRemCost-----:"+( sn.estRemCost ));
			for (Segment edge : node.getOutNeighbours() ){
				Node neighbour = edge.getEndNode();
				if (!nodeMap.containsKey(neighbour) ){
					SearchNode next = new SearchNode(neighbour, sn, sn.costSoFar + edge.getLength(),
							//		(sn.costSoFar+edge.getLength()) +
								neighbour.distanceTo(goal.getLoc(), Location.LINE));
					nodeMap.put(neighbour, next);
					points.add(neighbour);
					fringe.offer(next);
				}
			}

		}
		long endtime = System.currentTimeMillis();
		System.out.println("AStar cost time:"+ (endtime - starttime) + "ms. Search Node:"+points.size());

		while(endsn!=startsn){
			Node rightBackWards = endsn.node;
			Node fromNode = endsn.from.node;
			for(Segment s: rightBackWards.getInNeighbours()){
				if(s.getStartNode()== fromNode){
					endsn = endsn.from;
					searchPath.add(s);
				}
			}
		}
		return searchPath;
	}

	public List<Segment> searchDijkstra(Node start, Node goal, List<Node> points) {
		if (start==null || goal == null) return null;
		System.out.println("the start Node is "+start.toString() + " the goal Node is "+goal.toString());
		long starttime = System.currentTimeMillis();
		searchPath = new ArrayList<Segment>();
		PriorityQueue<SearchNode> fringe = new PriorityQueue<SearchNode>();
		SearchNode startsn = new SearchNode(start, null, 0);
		fringe.add(startsn);
		System.out.println("please wait loading......");
		path = new ArrayList<Node>();
		HashMap<Node, SearchNode> nodeMap = new HashMap<Node, SearchNode>();
		nodeMap.put(start,startsn);
		SearchNode endsn = null;
		while (! fringe.isEmpty()){
			SearchNode sn = fringe.poll();
			Node node = sn.node;
			if (node==goal) {
				endsn = sn;
				System.out.println("The node your looking for is " + node.toString());
				System.out.println("costSoFar is :" + ( sn.costSoFar + sn.estRemCost ));
				break;
			}
			if (sn.visited) continue;
			sn.visited = true;
			for (Segment edge : node.getOutNeighbours() ){
				Node neighbour = edge.getEndNode();
				if (!nodeMap.containsKey(neighbour)){
					SearchNode next = new SearchNode(neighbour, sn, sn.costSoFar + edge.getLength());
					nodeMap.put(neighbour, next);
					fringe.offer(next);
					points.add(neighbour);
				}
			}

		}
		long endtime = System.currentTimeMillis();
		System.out.println("AStar cost time:"+ (endtime - starttime) + "ms. Search Node:"+points.size());
		while(endsn!=startsn){
			Node rightBackWards = endsn.node;
			Node fromNode = endsn.from.node;
			for(Segment s: rightBackWards.getInNeighbours()){
				if(s.getStartNode()== fromNode){
					endsn = endsn.from;
					searchPath.add(s);
				}
			}
		}
		return searchPath;
	}


//    public ArrayList<Node> getPath(){
//    	return path;
//    }





    public void setSearchPath(List<Segment> searchPath) {
		this.searchPath = searchPath;
	}


}
