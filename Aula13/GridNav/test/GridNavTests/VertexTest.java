package GridNavTests;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import GridNav.Vertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Vertex class.
 * @author EliAir
 */
public class VertexTest {
    private Vertex v;
    
    public VertexTest() {
    }
    
    @Before
    public void setUp() {
        v=new Vertex(0,0,'.');
    }
    
    /**
     * Constructor should set correct initial values.
     * Do not change initial values, search algorithms and ImageBuilder depend on them.
     */
    
    @Test
    public void testConstructor() {
        assertEquals(0, v.getX());
        assertEquals(0, v.getY());
        assertEquals('.', v.getKey());
        assertEquals(-1, v.getDistance(), 0.002);
        assertEquals(-1, v.getToGoal(), 0.002);
        assertFalse(v.isOnPath());       
        assertNull(v.getPath());
    }
    
    /**
     * Equals should return true if coordinates match.
     */
    
    @Test
    public void testEquals(){
        Vertex comp = new Vertex(0,0,'.');
        assertTrue(v.equals(comp));
        comp.setX(1);
        assertFalse(v.equals(comp));
    }
    
    /**
     * Compare to should order items based on whose distance is smaller.
     * Smaller distance -> smaller in natural ordering -> return -1 when smaller distance compared to larger.
     */
    
    @Test
    public void testCompareTo(){        
        Vertex comp = new Vertex(0,0,'.');
        
        v.setDistance(1);
        comp.setDistance(2);        
        assertEquals(-1, v.compareTo(comp));
        
        comp.setDistance(0);
        assertEquals(1, v.compareTo(comp));
        
        comp.setDistance(1);
        assertEquals(0, v.compareTo(comp));
    }
    
    /**
     * CompareTo must work correctly with astar manhattan when distance is Integer.MAX_VALUE. 
     */
    
    @Test
    public void testCompareToInSpecialCases(){
        Vertex comp = new Vertex(0,0,'.');
                        
        v.setDistance(Integer.MAX_VALUE);
        comp.setDistance(Integer.MAX_VALUE);
        
        assertEquals(0, v.compareTo(comp));
                
        v.setToGoal(1);        
        comp.setToGoal(2);
        assertEquals(-1, v.compareTo(comp));
        
        comp.setToGoal(0);
        assertEquals(1, v.compareTo(comp));
        
        
        comp.setDistance(100);
        comp.setToGoal(0);
        assertEquals(1, v.compareTo(comp));
        
        
    }
}