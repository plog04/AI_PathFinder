package newBoite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;


import newBoite.AStarPathFinder;
import newBoite.Path;
//import newBoite.AStarPathFinder.Node;
import newBoite.PathFinder;

/**
 * A simple test to show some path finding at unit
 * movement for a tutorial at http://www.cokeandcode.com
 * 
 * @author Kevin Glass
 */
public class PathTest extends JFrame {
	/** The map on which the units will move */
	private GameMap map = new GameMap();
	/** The path finder we'll use to search our map */
	public PathFinder finder;
	/** The last path found for the current unit */
	private Path path;
	private ArrayList<Node> nodesCheck;
	private int tileSize = 32;
	private int screenHeight = 1080;
	private int screenWidth = 1080;
	
	/** The list of tile images to render the map */
	private Image[] tiles = new Image[6];
	/** The offscreen buffer used for rendering in the wonder world of Java 2D */
	private Image buffer;
	
	/** The x coordinate of selected unit or -1 if none is selected */
	private int selectedx = -1;
	/** The y coordinate of selected unit or -1 if none is selected */
	private int selectedy = -1;
	
	/** The x coordinate of the target of the last path we searched for - used to cache and prevent constantly re-searching */
	private int lastFindX = -1;
	/** The y coordinate of the target of the last path we searched for - used to cache and prevent constantly re-searching */
	private int lastFindY = -1;
	
	/**
	 * Create a new test game for the path finding tutorial
	 */
	public PathTest() {
		super("Path Finding Example");
	
		try {
			tiles[GameMap.TREES] = ImageIO.read(getResource("res/trees.png"));
			tiles[GameMap.GRASS] = ImageIO.read(getResource("res/grass.png"));
			tiles[GameMap.WATER] = ImageIO.read(getResource("res/water.png"));
			tiles[GameMap.TANK] = ImageIO.read(getResource("res/tank.png"));
			tiles[GameMap.PLANE] = ImageIO.read(getResource("res/plane.png"));
			tiles[GameMap.BOAT] = ImageIO.read(getResource("res/boat.png"));
		} catch (IOException e) {
			System.err.println("Failed to load resources: "+e.getMessage());
			System.exit(0);
		}
		
		finder = new AStarPathFinder(map, 500, false);
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				handleMousePressed(e.getX(), e.getY());
			}
		});
		/*addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
			}

			public void mouseMoved(MouseEvent e) {
				handleMouseMoved(e.getX(), e.getY());
			}
		});*/
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		setSize(screenWidth,screenHeight);
		setResizable(true);
		setVisible(true);
	}
	
	/**
	 * Load a resource based on a file reference
	 * 
	 * @param ref The reference to the file to load
	 * @return The stream loaded from either the classpath or file system
	 * @throws IOException Indicates a failure to read the resource
	 */
	private InputStream getResource(String ref) throws IOException {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ref);
		if (in != null) {
			return in;
		}
		
		return new FileInputStream(ref);
	}

	/**
	 * Handle the mouse being moved. In this case we want to find a path from the
	 * selected unit to the position the mouse is at
	 * 
	 * @param x The x coordinate of the mouse cursor on the screen
	 * @param y The y coordinate of the mouse cursor on the screen
	 */
	private void handleMouseMoved(int x, int y) {
		//Coordonnée p/r a l'origine situé a (50,50)
		x -= 50;
		y -= 50;
		//Coordonnée en unité tile
		x /= tileSize;
		y /= tileSize;
		
		if ((x < 0) || (y < 0) || (x >= map.getWidthInTiles()) || (y >= map.getHeightInTiles())) {
			return;
		}
		
		if (selectedx != -1) {
			if ((lastFindX != x) || (lastFindY != y)) {
				lastFindX = x;
				lastFindY = y;
				path = finder.findPath(new UnitMover(map.getUnit(selectedx, selectedy)), 
									   selectedx, selectedy, x, y);
				if(path!=null)
					nodesCheck=finder.getClosedList();
				else
					nodesCheck=null;
				repaint(0);
			}
		}
	}
	/**
	 * Handle the mouse being pressed. If the mouse is over a unit select it. Otherwise we move
	 * the selected unit to the new target (assuming there was a path found)
	 * 
	 * @param x The x coordinate of the mouse cursor on the screen
	 * @param y The y coordinate of the mouse cursor on the screen
	 */
	private void handleMousePressed(int x, int y) {
		//Coordonnée p/r a l'origine situé a (50,50)
		x -= 50;
		y -= 50;
		//Coordonnée en unité tile
		x /= tileSize;
		y /= tileSize;
		
		if ((x < 0) || (y < 0) || (x >= map.getWidthInTiles()) || (y >= map.getHeightInTiles())) {
			return;
		}
		
		if (map.getUnit(x, y) != 0) {
			selectedx = x;
			selectedy = y;
			lastFindX = - 1;
		} else {
			if (selectedx != -1) {
				map.clearVisited();
				path = finder.findPath(new UnitMover(map.getUnit(selectedx, selectedy)), 
						   			   selectedx, selectedy, x, y);
				nodesCheck = finder.getClosedList();
				if (path != null) {
					path = null;
					nodesCheck = null;
					int unit = map.getUnit(selectedx, selectedy);
					map.setUnit(selectedx, selectedy, 0);
					map.setUnit(x,y,unit);
					selectedx = x;
					selectedy = y;
					lastFindX = - 1;
				}
			}
		}
		
		repaint(0);
	}
	
	/**
	 * @see java.awt.Container#paint(java.awt.Graphics)
	 */
	public void paint(Graphics graphics) {	
		// create an offscreen buffer to render the map
		if (buffer == null) {
			buffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);			
		}
		Graphics g = buffer.getGraphics();
		
		g.clearRect(0,0,screenWidth,screenHeight);
		g.translate(50, 50);	//Deplacement de l'origine du systeme au point (50,50)
		
		// cycle through the tiles in the map drawing the appropriate
		// image for the terrain and units where appropriate
	
				
		for (int x=0;x<map.getWidthInTiles();x++) {
			for (int y=0;y<map.getHeightInTiles();y++) {
				g.drawImage(tiles[map.getTerrain(x, y)],x*tileSize,y*tileSize,null);
				if (map.getUnit(x, y) != 0) {
					g.drawImage(tiles[map.getUnit(x, y)],x*tileSize,y*tileSize,null);
				} else {
					//if (finder.getClosedList() != null){
					if (nodesCheck != null){	
						for(Node n:nodesCheck){
							if (n.contains(x,y)){
								g.setColor(Color.red);
								g.fillRect((x*tileSize)+4, (y*tileSize)+4,7,7);
								break;
							}
						}
					}
					if (path != null) {
						if (path.contains(x, y)) {
							g.setColor(Color.blue);
							g.fillRect((x*tileSize)+4, (y*tileSize)+4,7,7);
						}
					}
					
				}
			}
		}

		// if a unit is selected then draw a box around it
		if (selectedx != -1) {
			g.setColor(Color.black);
			g.drawRect(selectedx*tileSize, selectedy*tileSize, tileSize-1, tileSize-1);
			g.drawRect((selectedx*tileSize)-2, (selectedy*tileSize)-2, tileSize +3,tileSize +3);
			g.setColor(Color.white);
			g.drawRect((selectedx*tileSize)-1, (selectedy*tileSize)-1, tileSize +1,tileSize +1);
		}
		
		// finally draw the buffer to the real graphics context in one
		// atomic action
		graphics.drawImage(buffer, 0, 0, null);
	}
	
	public static void writeToFile (String content, File file){
		try {
			  
			// Si le fichier n'existe pas, créons-le
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Entry point to our simple test game
	 * 
	 * @param argv The arguments passed into the game
	 */
	public static void main(String[] argv) {
		File file = new File("TestA.txt");
		String content = new String();
		Path path;
		int n=0;	
		PathTest test = new PathTest();	
		
		//Contenu des resultats d'analyse pour un test en particuler
		/*content = "Essaie; Depart; Arrivee; cheminTrouvee (pas); NbreCaseEssaye(echec);\n";	
		for (int i = 0; i<15;i++){
			for (int j = 0; j<30;j++){
				path = test.finder.findPath(new UnitMover(test.map.getUnit(15, 3)),i, 2, j, 18);			    
				content += n +";";
				content += "( "+i+","+2+" )"+";";
				content += "( "+j+","+18+" )"+ ";";
				if(path==null){
					content += "null" +";";
					content += "null" +";\n";
				}
				else{
				content += path.getLength() +";";
				content += test.finder.getClosedList().size() + ";\n";
				}
				
				n++;
				
			}
		}*/
		
		//Contenu des resultats d'analyse pour un ensemble de test
		content = "Essaie; Poid; TotalTest; totalCheminNonTrouver; totalPath (pas);totalCaseEssayerEchec;\n";
		test.finder.setHeuristicType(0);
		int totalTest=0;
		int totalPath=0;
		int totalCaseEssayerEchec = 0;
		int totalCheminNonTrouver =0;
		test.finder.setHeuristicWeight((float)(1.2));
		for (float z=0;z<5;z=(float)(z+0.2)){
			totalTest=0;
			totalPath=0;
			totalCaseEssayerEchec = 0;
			totalCheminNonTrouver = 0;
			test.finder.setHeuristicWeight(z);
			for (int i = 0; i<15;i++){
				for (int j = 0; j<30;j++){
					path = test.finder.findPath(new UnitMover(test.map.getUnit(15, 3)),i, 2, j, 18);
					totalTest++;
					if(path==null)
						totalCheminNonTrouver++;
					else{	
						totalPath=totalPath+path.getLength();
						totalCaseEssayerEchec=totalCaseEssayerEchec+test.finder.getClosedList().size();
					}
				}
			}
			content += n +";";
			content += z +";";
			content += totalTest+";";
			content += totalCheminNonTrouver+ ";";
			content += totalPath+ ";";
			content += totalCaseEssayerEchec+ ";\n";
			n++;
		}
		writeToFile(content,file );	
	}
}
