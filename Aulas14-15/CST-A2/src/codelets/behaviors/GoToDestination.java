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

    private MemoryObject brickListMO;
    private MemoryObject innerSenseMO;
    private MemoryObject legsMO;
    private final int creatureBasicSpeed;
    private final double reachDistance;

    // For the path in the environment
    private final Pathfinder planner;
    private final double dimX;
    private final double dimY;
    private final double destX;
    private final double destY;
    private final static double MIN_DIST = 20.0;

    public GoToDestination(int creatureBasicSpeed, int reachDistance) {
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.reachDistance = reachDistance;

        // Get World dimensions
        dimX = World.getInstance().getEnvironmentWidth();
        dimY = World.getInstance().getEnvironmentHeight();

        // Set the final destination as the corner at the bottom right, ie xDim, yDim.
        destX = dimX;
        destY = dimY;

        planner = new Pathfinder((int) dimX, (int) dimY);
    }

    @Override
    public void accessMemoryObjects() {
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        brickListMO = (MemoryObject) this.getInput("BRICK_LIST");
        legsMO = (MemoryObject) this.getOutput("LEGS");
    }

    @Override
    public void proc() {

        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
        // Check if the creature is close to the destination
        if (cis.position == null) {
            return;
        }
        double crX = cis.position.getX();
        double crY = cis.position.getY();
        if (calculateDistance(crX, crY, destX, destY) < MIN_DIST) {
            // Already arrived, return.
            return;
        } else {
            // Need to follow a plan to get to the destination.
            if (!planner.hasPlan()) {
                // There is no plan, create one.
                List<Thing> brickList = (List<Thing>) brickListMO.getI();
                planner.replan((int) crX, (int) crY, (int) destX, (int) destY, brickList);
                // Now get the first destination in the plan
                int[] step = planner.getNextDestination();
                if (step[0] == -1) {
                    // No more steps
                } else {
                    JSONObject message = new JSONObject();
                    // Instruct creature to follow the current step.
                    try {
                        message.put("ACTION", "GOTO");
                        message.put("X", (int) step[0]);
                        message.put("Y", (int) step[1]);
                        message.put("SPEED", creatureBasicSpeed);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    legsMO.setI(message.toString());
                }

            }

        }

    }//end proc

    @Override
    public void calculateActivation() {

    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }
}
