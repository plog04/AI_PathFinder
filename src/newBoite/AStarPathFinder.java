package newBoite;

import java.util.ArrayList;
import java.util.Collections;

import newBoite.Node;
import newBoite.ClosestHeuristic;

/**
 * A path finder implementation that uses the AStar heuristic based algorithm
 * to determine a path. 
 * 
 * @author Kevin Glass
 */
public class AStarPathFinder implements PathFinder {
	/** The set of nodes that have been searched through */
	private ArrayList<Node> closed = new ArrayList<Node>();
	/** The set of nodes that we do not yet consider fully searched */
	private SortedList open = new SortedList();
	
	/** The map being searched */
	private TileBasedMap map;
	/** The maximum depth of search we're willing to accept before giving up */
	private int maxSearchDistance;
	
	/** The complete set of nodes across the map */
	private Node[][] nodes;
	/** True if we allow diaganol movement */
	private boolean allowDiagMovement;
	/** The heuristic we're applying to determine which nodes to search first */
	private AStarHeuristic heuristic;
	private int heuristicType;
	private float heuristicWeight;
	
	/**
	 * Create a path finder with the default heuristic - closest to target.
	 * 
	 * @param map The map to be searched
	 * @param maxSearchDistance The maximum depth we'll search before giving up
	 * @param allowDiagMovement True if the search should try diaganol movement
	 */
	public AStarPathFinder(TileBasedMap map, int maxSearchDistance, boolean allowDiagMovement) {
		this(map, maxSearchDistance, allowDiagMovement, new ClosestHeuristic());
	}

	/**
	 * Create a path finder 
	 * 
	 * @param heuristic The heuristic used to determine the search order of the map
	 * @param map The map to be searched
	 * @param maxSearchDistance The maximum depth we'll search before giving up
	 * @param allowDiagMovement True if the search should try diaganol movement
	 */
	public AStarPathFinder(TileBasedMap map, int maxSearchDistance, 
						   boolean allowDiagMovement, AStarHeuristic heuristic) {
		this.heuristic = heuristic;
		this.heuristicType=0;
		this.heuristicWeight=1;
		this.map = map;
		this.maxSearchDistance = maxSearchDistance;
		this.allowDiagMovement = allowDiagMovement;
		
		nodes = new Node[map.getWidthInTiles()][map.getHeightInTiles()];
		for (int x=0;x<map.getWidthInTiles();x++) {
			for (int y=0;y<map.getHeightInTiles();y++) {
				nodes[x][y] = new Node(x,y);
			}
		}
	}
	
	/**
	 * @see PathFinder#findPath(Mover, int, int, int, int)
	 */
	public Path findPath(Mover mover, int sx, int sy, int tx, int ty) {
		// Si la case destination est un obstacle, alors annulle la recherche.
		if (map.blocked(mover, tx, ty)) {
			return null;
		}
		
		//Initialisation de l'etat de l'agent, la liste Closed (Liste de noeud essayer et écarter de la solution) est vide,
		//la liste Open (Liste de noeud a evaluer pouvant potentiellement faire partie de la solution) ne contient que le noeud de depart avec
		//un cout de 0 (on est deja là) et une profondeur atteinte de 0 (pas fait de pas encore).
		nodes[sx][sy].setCost(0);
		nodes[sx][sy].setDepth(0); 
		closed.clear();
		open.clear();
		open.add(nodes[sx][sy]);
		
		nodes[tx][ty].setParent(null);
		
		// Tant que nous avons pas atteint la destination et depassé un nombre max de pas, faire:
		int maxDepth = 0;
		while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
			
			//Prendre le premier noeud de la liste. Il s'agit du prochain noeud à être explorer (ou reevaluer).
			Node current = getFirstInOpen();
			
			//Si le noeud à evaluer est le noeud destination alors arreter la recherche.
			if (current == nodes[tx][ty]) {
				break;
			}
			// On enleve le noeud de la liste Open et on la met dans la liste Closed
			removeFromOpen(current);
			addToClosed(current);
			
			//Rechercher chaque noeud voisin du noeud en evaluation et calculer leur cout 
			//si elle devienne le prochain noeud sur lequel se deplace l'agent			
			for (int x=-1;x<2;x++) {
				for (int y=-1;y<2;y++) {
					// Ce noeud est celui actuellement sous evaluation, il n'est donc pas un voisin
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					//Si on interdit les deplacement en diagonale alors 
					//les voisins en coin sont ignorer
					if (!allowDiagMovement) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}					
					
					//Localison le noeud voisin sur le terrain
					int xp = x + current.getX();
					int yp = y + current.getY();
					
					//Si le noeud voisin est une case de deplacement potentiel valide, 
					//ie si c'est une case sans obstacle, a l'interieur du terrain et n'est pas le noeud de depart.
					if (isValidLocation(mover,sx,sy,xp,yp)) {
						
						//Le cout pour atteindre ce noeud voisin est le cout du noeud en evaluation plus le cout du mouvement. 
						//Ce calcul n'inclue que la partie Disjstra (g(n)) de A*
						float nextStepCost = current.getCost() + getMovementCost(mover, current.getX(), current.getY(), xp, yp); 
						Node neighbour = nodes[xp][yp];
						map.pathFinderVisited(xp, yp);
						
						//Si le nouveau cout (g(n)) calculer pour le noeud voisin est moindre que 
						//son ancien cout calculer, alors s'assurer que le noeud n'est pas 
						//dans la liste Closed. Nous avons trouver un meilleur chemin pour nous
						//rendre a ce noeud. Il faut donc reevaluer son f(n):
						//Si le noeud faisait deja partie de la liste Open, nous allons le retirer 
						//pour le temps de la reevaluation.
						if (nextStepCost < neighbour.getCost()) {
							if (inOpenList(neighbour)) {
								removeFromOpen(neighbour);
							}
							if (inClosedList(neighbour)) {
								removeFromClosed(neighbour);
							}
						}
												
						//Si le noeud ne fait pas partie de la liste de noeud candidate a evaluer(Open) ou de la liste des noeud rejeter (Closed)
						//alors calculer et assigner son nouveau cout g(n) et h(n) et l'ajouter à la liste Open.
						if (!inOpenList(neighbour) && !(inClosedList(neighbour))) {
							neighbour.setCost(nextStepCost);
							neighbour.setHeuristic(getHeuristicCost(mover, xp, yp, tx, ty, sx, sy));
							maxDepth = Math.max(maxDepth, neighbour.setParent(current));
							addToOpen(neighbour);
						}
					}
				}
			}
		}
				
		//Nous n'avons pas atteint la destination dans notre recherche. Retourne null.
		if (nodes[tx][ty].getParent() == null) {
			return null;
		}
				
		//Nous avons atteint la destination, retourner le chemin trouver.
		//Utiliser les references au parent pour faire le chemin du retour et reconstituer l'ensemble des 
		//noeuds empruntés
		Path path = new Path();
		Node target = nodes[tx][ty];
		while (target != nodes[sx][sy]) {
			path.prependStep(target.getX(), target.getY());
			target = target.getParent();
		}
		path.prependStep(sx,sy);
		
		//Give the length of the path
		
		System.out.println("Nombre de pas du chemin : "+ path.getLength());
		System.out.println("Nombre case essayé sans succes : "+ this.getClosedList().size());
		
		// thats it, we have our path 
		return path;
	}

	/**
	 * Get the first element from the open list. This is the next
	 * one to be searched.
	 * 
	 * @return The first element in the open list
	 */
	protected Node getFirstInOpen() {
		return (Node) open.first();
	}
	
	/**
	 * Add a node to the open list
	 * 
	 * @param node The node to be added to the open list
	 */
	protected void addToOpen(Node node) {
		open.add(node);
	}
	
	/**
	 * Check if a node is in the open list
	 * 
	 * @param node The node to check for
	 * @return True if the node given is in the open list
	 */
	protected boolean inOpenList(Node node) {
		return open.contains(node);
	}
	
	public int getOpenListSize() {
		return open.size();
	}
	
	/**
	 * Remove a node from the open list
	 * 
	 * @param node The node to remove from the open list
	 */
	protected void removeFromOpen(Node node) {
		open.remove(node);
	}
	
	/**
	 * Add a node to the closed list
	 * 
	 * @param node The node to add to the closed list
	 */
	protected void addToClosed(Node node) {
		closed.add(node);
	}
	
	/**
	 * get the closed list
	 * 
	 * @param node The node to add to the closed list
	 */
	public ArrayList<Node> getClosedList() {
		return closed;
	}
	
	/**
	 * Check if the node supplied is in the closed list
	 * 
	 * @param node The node to search for
	 * @return True if the node specified is in the closed list
	 */
	protected boolean inClosedList(Node node) {
		return closed.contains(node);
	}
		
	/**
	 * Remove a node from the closed list
	 * 
	 * @param node The node to remove from the closed list
	 */
	protected void removeFromClosed(Node node) {
		closed.remove(node);
	}
	
	/**
	 * Check if a given location is valid for the supplied mover
	 * 
	 * @param mover The mover that would hold a given location
	 * @param sx The starting x coordinate
	 * @param sy The starting y coordinate
	 * @param x The x coordinate of the location to check
	 * @param y The y coordinate of the location to check
	 * @return True if the location is valid for the given mover
	 */
	protected boolean isValidLocation(Mover mover, int sx, int sy, int x, int y) {
		boolean invalid = (x < 0) || (y < 0) || (x >= map.getWidthInTiles()) || (y >= map.getHeightInTiles());
		
		if ((!invalid) && ((sx != x) || (sy != y))) {
			invalid = map.blocked(mover, x, y);
		}
		
		return !invalid;
	}
	
	/**
	 * Get the cost to move through a given location
	 * 
	 * @param mover The entity that is being moved
	 * @param sx The x coordinate of the tile whose cost is being determined
	 * @param sy The y coordiante of the tile whose cost is being determined
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @return The cost of movement through the given tile
	 */
	public float getMovementCost(Mover mover, int sx, int sy, int tx, int ty) {
		return map.getCost(mover, sx, sy, tx, ty);
	}

	/**
	 * Get the heuristic cost for the given location. This determines in which 
	 * order the locations are processed.
	 * 
	 * @param mover The entity that is being moved
	 * @param x The x coordinate of the tile whose cost is being determined
	 * @param y The y coordiante of the tile whose cost is being determined
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @return The heuristic cost assigned to the tile
	 */
	public float getHeuristicCost(Mover mover, int x, int y, int tx, int ty, int sx, int sy) {
		if(this.heuristicType==0)
			return heuristic.getCostManhattan(map, mover, this.heuristicWeight, x, y, tx, ty);
		else if (this.heuristicType==1){
			return heuristic.getCostWithDynamicWeighting(map, mover,this.heuristicWeight, x, y, tx, ty, sx, sy);
		}
		else
			return heuristic.getCost(map, mover, x, y, tx, ty);		
	}
	
	public void setHeuristicType(int i){
		heuristicType=i;
	}
	
	public void setHeuristicWeight(float w){
		this.heuristicWeight=w;
	}
	
	/**
	 * A simple sorted list
	 *
	 * @author kevin
	 */
	private class SortedList {
		/** The list of elements */
		private ArrayList list = new ArrayList();
		
		/**
		 * Retrieve the first element from the list
		 *  
		 * @return The first element from the list
		 */
		public Object first() {
			return list.get(0);
		}
		
		/**
		 * Empty the list
		 */
		public void clear() {
			list.clear();
		}
		
		/**
		 * Add an element to the list - causes sorting
		 * 
		 * @param o The element to add
		 */
		public void add(Object o) {
			list.add(o);
			Collections.sort(list);
		}
		
		/**
		 * Remove an element from the list
		 * 
		 * @param o The element to remove
		 */
		public void remove(Object o) {
			list.remove(o);
		}
	
		/**
		 * Get the number of elements in the list
		 * 
		 * @return The number of element in the list
 		 */
		public int size() {
			return list.size();
		}
		
		/**
		 * Check if an element is in the list
		 * 
		 * @param o The element to search for
		 * @return True if the element is in the list
		 */
		public boolean contains(Object o) {
			return list.contains(o);
		}
	}
}
