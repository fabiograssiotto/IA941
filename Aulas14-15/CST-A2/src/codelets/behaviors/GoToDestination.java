package codelets.behaviors;

import org.json.JSONException;
import org.json.JSONObject;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.List;
import memory.CreatureInnerSense;
import pathfinding.Pathfinder;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;

public class GoToDestination extends Codelet {

    private MemoryObject newBrickFoundMO;
    private MemoryObject brickListMO;
    private MemoryObject innerSenseMO;
    private MemoryObject legsMO;
    private final double creatureBasicSpeed;
    private int[] nextStep;

    // For the path in the environment
    private final Pathfinder planner;
    private final double dimX;
    private final double dimY;
    private final double destX;
    private final double destY;
    private final static double MIN_DIST = 5.0;

    public GoToDestination(double creatureBasicSpeed) {
        setTimeStep(20);
        this.creatureBasicSpeed = creatureBasicSpeed;

        // Get World dimensions
        dimX = World.getInstance().getEnvironmentWidth();
        dimY = World.getInstance().getEnvironmentHeight();

        // Set the final destination close to the corner at the bottom right.
        destX = 400;
        destY = 400;

        planner = new Pathfinder((int) dimX, (int) dimY);
    }

    @Override
    public void accessMemoryObjects() {
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        brickListMO = (MemoryObject) this.getInput("BRICK_LIST");
        newBrickFoundMO = (MemoryObject) this.getOutput("NEWBRICK_FOUND");
        legsMO = (MemoryObject) this.getOutput("LEGS");
    }

    @Override
    public void proc() {

        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
        List<Thing> brickList = (List<Thing>) brickListMO.getI();
        Boolean newBrickFound = (Boolean) newBrickFoundMO.getI();
        String command = "";

        // Check initialization
        if (cis.position == null || brickList == null) {
            return;
        }
        double crX = cis.position.getX();
        double crY = cis.position.getY();
        if (calculateDistance(crX, crY, destX, destY) < MIN_DIST) {
            // Already arrived, return.
            return;
        } else {
            // Need to follow a plan to get to the destination.
            if (newBrickFound) {
                planner.reset();
                Boolean reset = false;
                newBrickFoundMO.setI(reset);
                return;
            }
            if (!planner.hasPlan()) {
                // There is no plan, or we need a new one.
                planner.replan((int) crX, (int) crY, (int) destX, (int) destY, brickList);
                // Now get the first destination in the plan
                nextStep = planner.getNextDestination();
            } else {
                // Following a plan
                // Already got there?
                if (calculateDistance(crX, crY, nextStep[0], nextStep[1]) < MIN_DIST) {
                    // Go to the next step, then.
                    nextStep = planner.getNextDestination();
                }
            }
            if (nextStep[0] == -1) {
                // No more steps
            } else {
                command = goMove(nextStep[0], nextStep[1], creatureBasicSpeed);
                legsMO.setI(command);
            }
        }

    }//end proc

    @Override
    public void calculateActivation() {

    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    private String goMove(int x, int y, double speed) {
        JSONObject message = new JSONObject();
        // Instruct creature to follow the current step.
        try {
            System.out.println("GoToDestination [" + x + "," + y + "]");
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
