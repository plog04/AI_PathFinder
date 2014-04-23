package newBoite;

import java.util.ArrayList;
import newBoite.Node;
/**
 * A description of an implementation that can find a path from one 
 * location on a tile map to another based on information provided
 * by that tile map.
 * 
 * @see TileBasedMap
 * @author Kevin Glass
 */
public interface PathFinder {

	/**
	 * Trouve un chemin entre le noeud de depart(sx,sy) et de destination(tx,ty)
	 * tout en évitant les zones interdites et en minimisant le cout de deplacement
	 * f(n)
	 * @param mover Il s'agit de notre agent se deplacant, ce parametre contient les informations
	 * le concernant, ie les actuateurs et senseurs.
	 * @param sx Coordonnée x du noeud de depart
	 * @param sy Coordonnée y du noeud de depart
	 * @param tx Coordonnée x du noeud d'arrivee
	 * @param ty Coordonnée y du noeud d'arrivee
	 * @return Le chemin trouvee entre le noeud de depart et d'arrivee, sinon null si pas trouvee
	 */
	public Path findPath(Mover mover, int sx, int sy, int tx, int ty);
	public ArrayList<Node> getClosedList();
	public int getOpenListSize();
	public void setHeuristicType(int i);	
	public void setHeuristicWeight(float w);
}
