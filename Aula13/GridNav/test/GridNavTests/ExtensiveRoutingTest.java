package GridNavTests;

import GridNav.Options;
import GridNav.GridNav;
import GridNav.Cartographer;
import GridNav.Tools;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Heavy testing for all routing algorithms. 
 * Main idea: results of all three algorithms should be the same, always.
 * Warning: important to reload the map after every search, as the algorithms change the state of the VertexMatrix (the map)
 * @author Elias Nygren
 */
public class ExtensiveRoutingTest {
    private final static double EPSILON = 0.5;
    private Tools Tools;
    private GridNav la;
    private Cartographer c;
    private int[] t1;
    private int[] t2;
    private char[][] charM;
    
    @Before
    public void setUp() throws FileNotFoundException, Exception {
        Random r = new Random();
        la = new GridNav();
        charM = la.dotMapToCharMatrix(new File("./test/maps/combat.map"));
        la.loadCharMatrix(charM);      
        Tools = new Tools();
    }
    
    
    /**
     * Compares the results of all routing algorithms with diagonal allowed.
     * Uses EPSILON in comparing the double values. 
     * Manhattan is not tested as it is not consistent when diagonal is allowed.
     * @throws Exception 
     */
    @Test
    public void diagonalRoutingTest() throws Exception{
        int iterations = 100;
        diagonalHelper(Options.DIAGONAL_HEURISTIC, iterations);
        diagonalHelper(Options.EUCLIDEAN_HEURISTIC, iterations);
        
        
    }
    
    private void diagonalHelper(Options heuristic, int iterations) throws Exception{
        for(int i = 0; i<iterations; i++){
            
            //two random valid points on the map
            t1 = la.getRandomCoordinate();
            t2 = la.getRandomCoordinate();
            
            //LosAlgoritmos.route clears all changes on the map so no need to reload it in between.
            la.route(t1, t2, Options.DIJKSTRA, Options.NO_HEURISTIC, true);
            double dijkstra = la.getDistance();
            la.route(t1, t2, Options.ASTAR, heuristic, true);
            double astar = la.getDistance();
            la.route(t1, t2, Options.JPS, heuristic, true);
            double jps = la.getDistance();
            
            //if not dijkstra==astar==jps -> log error
            if(Math.abs(dijkstra-astar)>EPSILON || Math.abs(dijkstra-jps)>EPSILON){                
                System.out.println(dijkstra);
                System.out.println(astar);
                System.out.println(jps);
                System.out.println(""+Arrays.toString(t1)+","+Arrays.toString(t2));
                System.out.print("heuristic: ");
                if(heuristic==Options.DIAGONAL_HEURISTIC) System.out.println(Options.DIAGONAL_HEURISTIC);
                if(heuristic==Options.DIAGONAL_EQUAL_COST_HEURISTIC) System.out.println(Options.DIAGONAL_EQUAL_COST_HEURISTIC);
                if(heuristic==Options.EUCLIDEAN_HEURISTIC) System.out.println(Options.EUCLIDEAN_HEURISTIC);
            }
            
            assertEquals(dijkstra, astar, EPSILON);                
            assertEquals(dijkstra, jps, EPSILON);
            
            //this undoes all changes to the vertices left from running JPS so Tools.randomPoint works
            la.loadCharMatrix(charM);
            i++;
        }
    }
    
    
    
    /**
     * Dijkstra and Astar should have same MANHATTAN scores when diagonal not allowed.
     * JPS is not tested as it always uses 8 directions, MANHATTAN is consistent with only 4.
     * @throws Exception 
     */
    @Test
    public void manhattanTests() throws Exception{
        for (int i = 0; i < 100; i++) {
            //two random valid points on the map
            t1 = la.getRandomCoordinate();
            t2 = la.getRandomCoordinate();
            
            la.route(t1, t2, Options.DIJKSTRA, Options.NO_HEURISTIC, false);
            double dijkstra = la.getDistance();
            la.route(t1, t2, Options.ASTAR, Options.MANHATTAN_HEURISTIC, false);
            double astar = la.getDistance();
                  
            //if not dijkstra==astar -> log error
            if(Math.abs(dijkstra-astar)>EPSILON ){
                System.out.println(dijkstra);
                System.out.println(astar);
                System.out.println(""+Arrays.toString(t1)+","+Arrays.toString(t2));    
            } 
            
            assertEquals(dijkstra, astar, EPSILON);     
            
            //this undoes all changes to the vertices left from running JPS so Tools.randomPoint works
            la.loadCharMatrix(charM);
            
        }        
    }
}
