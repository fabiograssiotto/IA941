/*
 * Copyright (C) 2018 f.grassiotto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package pathfinding;

// main API for the library
import GridNav.GridNav;
// best route is given as a stack of vertices (contain coordinates, distance etc.)
import GridNav.Vertex;
// enums for selecting algorithm and heuristic
import GridNav.Options;
import java.util.ArrayDeque;
import java.util.List;
import ws3dproxy.model.Thing;

public final class Pathfinder {

    final private char[][] charMatrix;
    private ArrayDeque<Vertex> bestroute;
    private int xGrid = 0;
    private int yGrid = 0;
    private boolean hasPlan = false;

    // Factor to reduce the environment to a grid representation.
    private static final int GRID_FACTOR = 20;

    public Pathfinder(int xDim, int yDim) {

        // Creates grid with free movement spots (marked as '.')
        // representing the environment.
        xGrid = xDim / GRID_FACTOR;
        yGrid = yDim / GRID_FACTOR;

        charMatrix = new char[yGrid][xGrid];
        reset();
    }

    private void resetGrid(int xDim, int yDim) {
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                charMatrix[j][i] = '.';
            }
        }
    }

    public void addBrick(int x1, int x2, int y1, int y2) {
        int x1Grid = x1 / GRID_FACTOR;
        int x2Grid = x2 / GRID_FACTOR;
        int y1Grid = y1 / GRID_FACTOR;
        int y2Grid = y2 / GRID_FACTOR;

        for (int i = x1Grid - 1; i <= x2Grid + 1; i++) {
            for (int j = y1Grid - 1; j <= y2Grid + 1; j++) {
                // Sets bricks in the grid.
                if (i >= 0 && j >= 0 && i < xGrid && j < yGrid) {
                    // to avoid running out of bounds.
                    charMatrix[j][i] = 'b';
                }
            }
        }
    }

    private void findRoute(int x1Grid, int y1Grid, int x2Grid, int y2Grid) {

        GridNav gn = new GridNav();
        gn.loadCharMatrix(charMatrix);

        // Get the best route as a stack of Vertex objects:
        int[] start = {y1Grid, x1Grid}; // y, x
        int[] goal = {y2Grid, x2Grid};  // y, x
        bestroute = gn.route(start, goal, Options.ASTAR, Options.NO_HEURISTIC, false);
    }

    public int[] getNextDestination() {
        int[] ret = new int[2];
        if (bestroute == null) {
            ret[0] = -1;
            ret[1] = -1;
            return ret;
        }

        if (bestroute.isEmpty()) {
            ret[0] = -1;
            ret[1] = -1;
        } else {
            // Returns next destination, using environment coordinates.
            Vertex v = bestroute.pop();
            ret[0] = GRID_FACTOR * v.getX();
            ret[1] = GRID_FACTOR * v.getY();
        }

        return ret;
    }

    public void replan(int xOrg, int yOrg, int xDest, int yDest, List<Thing> l) {
        // Creates a plan for reaching the destination.
        reset();
        // Adds bricks to the grid
        for (Thing brick : l) {
            addBrick((int) brick.getX1(), (int) brick.getX2(), (int) brick.getY1(), (int) brick.getY2());
        }
        System.out.println("Pathfinder New Route: [" + xOrg + "," + yOrg + "] => [" + xDest + "," + yDest + "]");
        findRoute(xOrg / GRID_FACTOR, yOrg / GRID_FACTOR, xDest / GRID_FACTOR, yDest / GRID_FACTOR);
        hasPlan = true;
    }

    public boolean hasPlan() {
        return hasPlan;
    }

    public void reset() {
        hasPlan = false;
        resetGrid(xGrid, yGrid);
        if (bestroute != null) {
            bestroute.clear();
        }
    }
}
