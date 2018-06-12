package GridNavTests;




import GridNav.GridNav;
import GridNav.Cartographer;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Cartographer functionality.
 * @author Elias Nygren
 */
public class CartographerFunctionalityTest {
    private Cartographer c;
    private File f1;
    private File f2;    
    private File f3;
    private File f4;    
    private GridNav gn;

    public CartographerFunctionalityTest() {
    }
    
    @Before
    public void setUp() {      
        gn = new GridNav();
        f1 = new File("./test/maps/test1.map");
        f2 = new File("./test/maps/test2.map");
        f3 = new File("./test/maps/test3.map");
        f4 = new File("asdasdasd"); //does not exist        
    }
    
    @After
    public void tearDown() {
    }
    

    /**
     * toCharMatrix throws exception with invalid file.
     */
    @Test
    public void toCharMatrixThrowsExceptionCorrectly() {        
        try {   
            gn.dotMapToCharMatrix(f1);
        } catch (Exception e){
            assert false;
        }
        
        try {
            gn.dotMapToCharMatrix(f4);
            assert false;
        } catch (Exception e){
            assert true;
        }   
        
        try{
            gn.dotMapToCharMatrix(f3);            
            assert false;
        } catch (Exception e){
            assert true;
        }
    }
        
    /**
     * To charMatrix creates the char matrix correctly.
     */
    @Test
    public void toCharMatrixWorksCorrectly(){
        try {
            char[][] a = gn.dotMapToCharMatrix(f1);
            assertTrue(a[0][0]=='T');
            assertTrue(a[1][0]=='@');
            
            a = gn.dotMapToCharMatrix(f2);
            assertTrue(a[0][0]=='@');
            assertTrue(a[0][3]=='T');
            assertTrue(a[1][0]=='T');                        
            assertTrue(a[2][3]=='T');                        
        } catch (Exception ex) {
            assert false;
        }
        
        
    }
    
    
}