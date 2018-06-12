package GridNavTests;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import GridNav.Vertex;
import GridNav.VertexMinHeap;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for VertexMinHeap.
 * @author EliAir
 */
public class VertexMinHeapTest {
    VertexMinHeap heap;
    Random r;
    
    public VertexMinHeapTest() {
    }
    
    @Before
    public void setUp() {
        heap = new VertexMinHeap(10);         
        r = new Random();
        for (int i = 0; i < 100; i++) {
            Vertex a = new Vertex(0,i,'.');
            a.setDistance(r.nextDouble()+1);
            heap.add(a);
        }
        Vertex b = new Vertex(0,-1,'.');
        b.setDistance(0);
        heap.add(b);
        assertEquals(101, heap.size());
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Heap should have correct size, that should decrease when polled.
     */
    @Test
    public void correctMinAndHeapSizeDecreases(){
        assertEquals(0, heap.poll().getDistance(), 0.002);
        assertEquals(100, heap.size());
    }
    
    /**
     * Heap should have every item in correct order.
     */
    @Test
    public void valuesInCorrectOrder() {        
        Comparable[] arr = heap.getHeap();
        
        for (int i = 1; i < 100; i++) {
            if(2*i>100) break;
            assertTrue(arr[i].compareTo(arr[2*i])<0);
            assertTrue(arr[i].compareTo(arr[2*i+1])<0);
        }
        
    }
    
    
    /**
     * Update should should preserve order and update the index of the vertex.
     */
    @Test
    public void update(){
        heap = new VertexMinHeap(10);
        Vertex a = new Vertex(0,0,'.');
        Vertex b = new Vertex(0,0,'.');
        Vertex c = new Vertex(0,0,'.');
        Vertex d = new Vertex(0,0,'.');
        a.setDistance(1);
        b.setDistance(3);
        c.setDistance(5);
        d.setDistance(7);
        heap.add(a);
        heap.add(b);
        heap.add(c);
        heap.add(d);
        
        assertEquals(a, heap.poll());
        a.setDistance(2);
        heap.add(a);
        d.setDistance(1);
        heap.update(d);
        assertEquals(d, heap.poll());
        
        
        
    }

}