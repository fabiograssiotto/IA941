package pathfinding;

// main API for the library
import GridNav.GridNav;
// best route is given as a stack of vertices (contain coordinates, distance etc.)
import GridNav.Vertex;
// enums for selecting algorithm and heuristic
import GridNav.Options;
import java.util.ArrayDeque;

public class Example {

    public Example() {

        GridNav gn = new GridNav();

        char[][] charMatrix = new char[][]{
            "....".toCharArray(),
            ".ww.".toCharArray(),
            ".ww.".toCharArray(),
            "....".toCharArray()
        };

        //new char[4][4];
        //charMatrix[0][0] = '.';
        //from char[][] Matrix:
        gn.loadCharMatrix(charMatrix);

        // Get the best route as a stack of Vertex objects:
        int[] start = {0, 0}; // y, x
        int[] goal = {3, 3};  // y, x
        ArrayDeque<Vertex> bestroute = gn.route(start, goal, Options.ASTAR, Options.NO_HEURISTIC, false);

        while (!bestroute.isEmpty()) {
            Vertex v = bestroute.pop();
            //do something with v
            System.out.println("(" + v.getX() + ", " + v.getY() + ")");
        }
    }

    public static void main(String[] args) {
        new Example();
    }

}
