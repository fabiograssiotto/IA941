package codelets.behaviors;

import main.Environment;
import org.json.JSONException;
import org.json.JSONObject;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import memory.CreatureInnerSense;
import pathfinding.Pathfinder;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;

public class GoToDestination extends Codelet {

    private MemoryObject closeBrickFoundMO;
    private MemoryObject brickListMO;
    private MemoryObject innerSenseMO;
    private MemoryObject legsMO;
    private final double creatureBasicSpeed;
    private int[] nextStep;
    private Mind mind;

    // For the path in the environment
    private final Pathfinder planner;
    private final double dimX;
    private final double dimY;
    private final double destX;
    private final double destY;
    private final static double MIN_DIST = 10.0;

    public GoToDestination(double creatureBasicSpeed, Mind mind) {
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.mind = mind;
        // Get World dimensions
        dimX = World.getInstance().getEnvironmentWidth();
        dimY = World.getInstance().getEnvironmentHeight();

        // Set the final destination close to the corner at the bottom right.
        destX = Environment.destinationX;
        destY = Environment.destinationY;

        planner = new Pathfinder((int) dimX, (int) dimY);
    }

    @Override
    public void accessMemoryObjects() {
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        brickListMO = (MemoryObject) this.getInput("BRICK_LIST");
        closeBrickFoundMO = (MemoryObject) this.getOutput("CLOSE_BRICK_FOUND");
        legsMO = (MemoryObject) this.getOutput("LEGS");
    }

    @Override
    public void proc() {

        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
        List<Thing> brickList = (List<Thing>) brickListMO.getI();
        Boolean closeBrickFound = (Boolean) closeBrickFoundMO.getI();
        String command = "";

        // Check initialization
        if (cis.position == null || brickList == null) {
            return;
        }
        double crX = cis.position.getX();
        double crY = cis.position.getY();
        if (calculateDistance(crX, crY, destX, destY) < MIN_DIST) {
            // Arrived at the destination.
            System.out.println("GoToDestination Arrived");
            command = stopMove((int) crX, (int) crY);
            legsMO.setI(command);
            mind.shutDown();
            return;
        } else {
            // Need to follow a plan to get to the destination.
            if (closeBrickFound) {
                System.out.println("GoToDestination New Brick Found: REPLAN");
                planner.reset();
                Boolean reset = false;
                closeBrickFoundMO.setI(reset);
                return;
            }
            if (!planner.hasPlan()) {
                // There is no plan, or we need a new one.
                System.out.println("GoToDestination Create New Plan");
                planner.replan((int) crX, (int) crY, (int) destX, (int) destY, brickList);
                // Now get the first destination in the plan
                nextStep = planner.getNextDestination();
            } else {
                // Following a plan
                // Already got there?
                if (calculateDistance(crX, crY, nextStep[0], nextStep[1]) < MIN_DIST) {
                    // Go to the next step, then.
                    System.out.println("GoToDestination Reached [" + nextStep[0] + "," + nextStep[1] + "]");
                    nextStep = planner.getNextDestination();
                    // Stop the creature to wait for the next step.
                    command = stopMove((int) crX, (int) crY);
                    legsMO.setI(command);
                    return;
                }
            }
            if (nextStep[0] == -1) {
                // Bad plan created
                System.out.println("GoToDestination No Route to Destination");
            } else {
                System.out.println("GoToDestination GoTo [" + nextStep[0] + "," + nextStep[1] + "]");
                command = goMove(nextStep[0], nextStep[1], creatureBasicSpeed);
                legsMO.setI(command);
            }
        }

    }//end proc

    @Override
    public void calculateActivation() {

    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        Point2D p1 = new Point();
        p1.setLocation(x1, y1);

        Point2D p2 = new Point();
        p2.setLocation(x2, y2);

        return p2.distance(p1);
    }

    private String goMove(int x, int y, double speed) {
        JSONObject message = new JSONObject();
        // Instruct creature to follow the current step.
        try {
            message.put("ACTION", "GOTO");
            message.put("X", x);
            message.put("Y", y);
            message.put("SPEED", speed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message.toString();
    }

    private String stopMove(int x, int y) {
        JSONObject message = new JSONObject();
        // Instruct creature to follow the current step.
        try {
            System.out.println("GoToDestination [" + x + "," + y + "]");
            message.put("ACTION", "GOTO");
            message.put("X", x);
            message.put("Y", y);
            message.put("SPEED", 0.0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message.toString();
    }
}
