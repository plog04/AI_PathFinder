package newBoite;

import newBoite.AStarHeuristic;
import newBoite.Mover;
import newBoite.TileBasedMap;

/**
 * A heuristic that uses the tile that is closest to the target
 * as the next best tile.
 * 
 * @author Kevin Glass
 */
public class ClosestHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(TileBasedMap, Mover, int, int, int, int)
	 */
	public float getCost(TileBasedMap map, Mover mover, int x, int y, int tx, int ty) {		
		float dx = tx - x;
		float dy = ty - y;
		
		float result = (float) (Math.sqrt((dx*dx)+(dy*dy)));
		
		return result;
	}
	
	/**
	 * Heuristique de A* : Le chemin de Manhattan
	 * 
	 * @param map Le terrain de tuile sur lequel le chemin est recherch�
	 * @param mover L'agent se d�placant sur le terrain, utile pour r�cup�rer comportement special
	 * @param D Le poid de l'heuristique
	 * @param x Coordonn�e en x de la case � �valuer
	 * @param y Coordonn�e en y de la case � �valuer
	 * @param tx Coordonn�e en x de la case � destination
	 * @param ty Coordonn�e en y de la case � destination
	 */
	public float getCostManhattan(TileBasedMap map, Mover mover,float D, int x, int y, int tx, int ty) {		
		float dx = Math.abs(tx - x);
		float dy = Math.abs(ty - y);
		
		return (float) ((D)*(dx +dy));
	}
	/**
	 * Heuristique de la variante de l'algorithme A* : Le Dynamic weigthing
	 * 
	 * @param map Le terrain de tuile sur lequel le chemin est recherch�
	 * @param mover L'agent se d�placant sur le terrain, utile pour r�cup�rer comportement special
	 * @param weight Le poid de l'heuristique
	 * @param x Coordonn�e en x de la case � �valuer
	 * @param y Coordonn�e en y de la case � �valuer
	 * @param tx Coordonn�e en x de la case � destination
	 * @param ty Coordonn�e en y de la case � destination
	 * @param sx Coordonn�e en x de la case � d�part
	 * @param sy Coordonn�e en y de la case � d�part
	 */
	public float getCostWithDynamicWeighting(TileBasedMap map, Mover mover,float weight, int x, int y, int tx, int ty, int sx, int sy) {		
		float dx = Math.abs(tx - x);
		float dy = Math.abs(ty - y);
		
		float px = Math.abs(tx - sx);
		float py = Math.abs(ty - sy);
		float weightX=1;
		float weightY=1;
		if (px>0)
			weightX = dx/px;
		if (py>0)
			weightY = dy/py;
					
		return weightX*dx + weightY*dy;
	}

}
