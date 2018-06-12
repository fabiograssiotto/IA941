package GridNavTests;

import GridNav.Options;
import GridNav.GridNav;
import GridNav.Vertex;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tiny routing tests.
 * @author Elias Nygren
 */
public class TinyRoutingTest {
    private GridNav la;
    private int[] t1;
    private int[] t2;
    ArrayDeque<Vertex> bestroute;
    private static double epsilon = 0.005;
    
    public TinyRoutingTest() {
    }
    
    
    @Before
    public void setUp() throws FileNotFoundException, Exception {
        la = new GridNav();
        char[][] M = la.dotMapToCharMatrix(new File("./test/maps/test4.map"));
        la.loadCharMatrix(M);
        t1 = new int[] {0,0};
        t2 = new int[] {2, 2};
    }
    
    /**
     * Route should find correct vertices.
     */
    @Test
    public void routeFindsCorrectRoute() throws Exception{
        int[][] route1 = {{0,0},{0,1},{1,1},{2,1},{2,2}};
        int[][] route2 = {{0,0},{2,2}};
        
        bestroute = la.route(t1, t2, Options.ASTAR, Options.MANHATTAN_HEURISTIC, false);        
        assertEquals(5, bestroute.size());
        testRoute(route1);
        
        bestroute = la.route(t1, t2, Options.DIJKSTRA, Options.MANHATTAN_HEURISTIC, false);
        assertEquals(5, bestroute.size());
        testRoute(route1);
        
        bestroute = la.route(t1, t2, Options.JPS, Options.EUCLIDEAN_HEURISTIC, true);
        assertEquals(2, bestroute.size());
        testRoute(route2);
        
    }
    
    private void testRoute(int[][] route){
        
        
        for (int[] is : route) {
            Vertex v = bestroute.pop();
            assertTrue(v.getY()==is[0]);
            assertTrue(v.getX()==is[1]);
        }
    }
    
    /**
     * Route should set vertex distances correctly.
     */
    
    @Test
    public void runSetsDistancesCorrectly(){     
        double[] distances1 = {0,1,2,3,4};        
        double[] distances2 = {0,2*Math.sqrt(2)}; //two times diagonal == 2*sqrt(2)
        
        bestroute = la.route(t1, t2, Options.ASTAR, Options.MANHATTAN_HEURISTIC, false);
        assertEquals(5, bestroute.size());
        testDistances(distances1);
        
        bestroute = la.route(t1, t2, Options.DIJKSTRA, Options.MANHATTAN_HEURISTIC, false);
        assertEquals(5, bestroute.size());
        testDistances(distances1);
        
        bestroute = la.route(t1, t2, Options.JPS, Options.EUCLIDEAN_HEURISTIC, true);
        assertEquals(2, bestroute.size());
        testDistances(distances2);
    }
    
    private void testDistances(double[] distances){
        for (double d : distances) {
            Vertex v = bestroute.pop();
            assertEquals(d, v.getDistance(), epsilon);
        }
    }
}