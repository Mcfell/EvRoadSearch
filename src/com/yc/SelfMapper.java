package com.yc;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcfell on 2016/12/28.
 */
public class SelfMapper {
    public static class selfMapper{

        private JFrame frame;
        public JComponent drawing;
        private JTextArea textOutput;
        private JTextField nameEntry;
        private int windowSize = 700;

        private RoadGraph roadGraph;
        private List<Node> points;

        private Node selectedNode;  // the currently selected node
        private Node secondSelectedNode;
        private List<Segment> selectedSegments;  // the currently selected road or path
        private List<Segment> path = new ArrayList<Segment>();
        private boolean loaded = false;

        // Dimensions for drawing
        double westBoundary ;
        double eastBoundary ;
        double southBoundary;
        double northBoundary;
        Location origin;
        double scale;



        public selfMapper(int num){
            setupInterface();
            roadGraph = new RoadGraph();
            textOutput.setText("creating data...");
            //while (dataDir==null){dataDir=getDataDir();}
            // dataDir = "/Users/mcfell/Documents/WorkFile/JavaProject/EvRoadSearch/src/";
            //textOutput.append("Loading from "+dataDir+"\n");
            textOutput.append(roadGraph.loadData(num));
            setupScaling();
            loaded = true;
            drawing.repaint();
        }

        private class DirectoryFileFilter extends FileFilter {
            public boolean accept(File f) {return f.isDirectory();}
            public String getDescription(){return "Directories only";}
        }

        private String getDataDir(){
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new DirectoryFileFilter());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(frame)!=JFileChooser.APPROVE_OPTION){return null;}
            return fc.getSelectedFile().getPath()+File.separator;
        }


        private void setupScaling(){
            double[] b = roadGraph.getBoundaries();
            westBoundary = b[0];
            eastBoundary = b[1];
            southBoundary = b[2];
            northBoundary = b[3];
            resetOrigin();
            /*
              System.out.printf("Boundaries: w %.2f, e %.2f, s %.2f, n %.2f%n",
              b[0], b[1], b[2], b[3]);
              System.out.printf("Scaling from %s @ %.5f,%n", origin, scale);
            */
        }

        private void setupInterface(){
            // Set up a window .
            frame = new JFrame("Auckland Map Search");
            frame.setSize(windowSize+200, windowSize);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Set up a JComponent in the window that we can draw on
            // When the JComponent tries to paint itself, it will call the redraw method
            //  in this PathDrawer class, passing a Graphics object to it.
            // The redraw method can draw whatever it wants on the Graphics object.
            // We can ask the JComponent to paint itself by calling drawing.repaint()
            //  Note that this merely requests that the drawing is repainted; it won't
            //  necessarily do it immediately.
            drawing = new JComponent(){
                protected void paintComponent(Graphics g){redraw(g);}
            };
            frame.add(drawing, BorderLayout.CENTER);

            // Setup a text area for output
            textOutput = new JTextArea(5, 100);
            textOutput.setEditable(false);
            JScrollPane textSP = new JScrollPane(textOutput);
            frame.add(textSP, BorderLayout.SOUTH);

            //Set up a panel for some buttons.
            //To get nicer layout, we would need a LayoutManager on the panel.
            JPanel panel = new JPanel();
            frame.add(panel, BorderLayout.NORTH);

            //Add a text label to the panel.
            //	panel.add(new JLabel("Click to select"));

            // Add  buttons to the panel.
            JButton button = new JButton("+");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){zoomIn();}});

            button = new JButton("-");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){zoomOut();}});

            button = new JButton("<");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){pan("left");}});

            button = new JButton(">");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){pan("right");}});

            button = new JButton("^");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){pan("up");}});

            button = new JButton("v");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){pan("down");}});

            button = new JButton("A* search");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){
                    points = new ArrayList<Node>();
                    path = roadGraph.searchAStar(selectedNode, secondSelectedNode,points);
                    drawing.repaint();
                }});

            button = new JButton("Dijkstra search");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){
                    points = new ArrayList<Node>();
                    path = roadGraph.searchDijkstra(selectedNode, secondSelectedNode,points);
                    drawing.repaint();
                }});

            button = new JButton("Articulation");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){
                    //art = new Articulation(roadGraph.getNodes());
                    //art.findArtPoints(selectedNode);
                    //points = art.getArtPts();
                    drawing.repaint();
                    path= null;}});


            button = new JButton("Reset");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){reset();}});

            nameEntry = new JTextField(20);
            panel.add(nameEntry);
            nameEntry.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    lookupName(nameEntry.getText());
                    drawing.repaint();
                }});

            button = new JButton("Quit");
            panel.add(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){System.exit(0);}});

            // Add a mouselistener to the drawing JComponent to respond to mouse clicks.
            drawing.addMouseListener(new MouseAdapter(){
                public void mouseReleased(MouseEvent e){
                    if(selectedNode==null){
                        selectedNode = findNode(e.getPoint());
                    }else{
                        secondSelectedNode = findNode(e.getPoint());
                    }

                    if (selectedNode!=null){
                        textOutput.setText(selectedNode.toString());
                    }
                    if (secondSelectedNode!=null){
                        textOutput.setText(selectedNode.toString()+ "\n" + secondSelectedNode.toString());
                    }

                    drawing.repaint();}});


            // Once it is all set up, make the interface visible
            frame.setVisible(true);

        }
        private double zoomFactor = 1.25;
        private double panFraction = 0.2;


        //The redraw method that will be called from the drawing JComponent and will
        //draw the map at the current scale and shift.
        public void redraw(Graphics g){
            if (roadGraph!= null && loaded ){
                roadGraph.redraw(g, origin, scale);
                if (selectedNode!=null) {
                    g.setColor(Color.green);
                    selectedNode.draw(g, origin, scale);}
                if(secondSelectedNode!=null){
                    g.setColor(Color.red);
                    secondSelectedNode.draw(g, origin, scale);
                }



                if (selectedSegments!=null){
                    g.setColor(Color.red);
                    for (Segment seg : selectedSegments){
                        seg.draw(g, origin, scale);

                    }
                }

                if(path!=null){
                    g.setColor(Color.RED);
                    for(Segment seg : path){
                        seg.draw(g, origin, scale);
                    }
                    ArrayList<String> temp = new ArrayList<String>();
                    for(Segment s : path){
                        if(!temp.contains(s.getRoad().toString())){
                            temp.add(s.getRoad().toString());
                        }
                    }
                    textOutput.setText(temp.toString());

                }

                if(points!=null){
                    g.setColor(Color.yellow);
                    for(Node n : points){
                        n.draw(g, origin, scale);
                    }
                }

            }

        }


        //set origin and scale for the whole map
        private void resetOrigin(){
            origin = new Location(westBoundary, northBoundary);
            scale = Math.min(windowSize/(eastBoundary-westBoundary),
                    windowSize/(northBoundary-southBoundary));
        }

        //shrink the scale (pixels/per km) by zoomFactor and move origin
        private void zoomOut(){
            scale =  scale/zoomFactor;
            double deltaOrig = windowSize/scale*(zoomFactor-1)/zoomFactor/2;
            origin = new Location(origin.x - deltaOrig, origin.y + deltaOrig);
            drawing.repaint();
        }
        //expand the scale (pixels/per km) by zoomFactor and move origin
        private void zoomIn(){
            double deltaOrig = windowSize/scale*(zoomFactor-1)/zoomFactor/2;
            origin = new Location(origin.x + deltaOrig, origin.y - deltaOrig);
            scale =  scale*zoomFactor;
            drawing.repaint();
        }

        private void pan(String dir){
            double delta = windowSize*panFraction/scale;

            if(dir.equals("left"))origin = new Location(origin.x-delta, origin.y);
            if(dir.equals("right"))origin = new Location(origin.x+delta, origin.y);
            if(dir.equals("up"))origin = new Location(origin.x, origin.y+delta);
            if(dir.equals("down"))origin = new Location(origin.x, origin.y-delta);
            drawing.repaint();
        }
        //Find the place that the mouse was clicked on (if any)
        private Node findNode(Point mouse){
            return roadGraph.findNode(mouse, origin, scale);
        }


        public void reset() {
            // TODO Auto-generated method stub
            resetOrigin();
            selectedNode = secondSelectedNode =  null;
            path = null;
            points = null;
            textOutput.setText("");
            roadGraph.setSearchPath(null);
            drawing.repaint();

        }

        private void lookupName(String query){
            List<String> names = new ArrayList(roadGraph.lookupName(query));
            if (names.isEmpty()){
                selectedSegments=null;
                textOutput.setText("Not found");
            }
            else if (names.size()==1){
                String fullName =names.get(0);
                nameEntry.setText(fullName);
                textOutput.setText("Found");
                selectedSegments=roadGraph.getRoadSegments(fullName);
            }
            else{
                selectedSegments=null;
                String prefix = maxCommonPrefix(query, names);
                nameEntry.setText(prefix);
                textOutput.setText("Options: ");
                for (int i = 0; i<10&&i<names.size(); i++){
                    textOutput.append(names.get(i));textOutput.append(", ");
                }
                if (names.size()>10){textOutput.append("...\n");}
                else { textOutput.append("\n"); }
            }
        }

        private String maxCommonPrefix(String query, List<String>names) {
            String ans = query;
            for (int i = query.length(); ; i++) {
                if (names.get(0).length() < i) return ans;
                String cand = names.get(0).substring(0, i);
                for (String name : names) {
                    if (name.length() < i) return ans;
                    if (name.charAt(i - 1) != cand.charAt(i - 1)) return ans;
                }
                ans = cand;
            }
        }
        public static void main(String[] arguments){
            selfMapper obj = new selfMapper(300);

        }


    }
}
