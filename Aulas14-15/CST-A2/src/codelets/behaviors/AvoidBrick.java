package codelets.behaviors;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

/*
* Removes when necessary objects that were not the target of the creature.
* Basically this is for objects (food/jewels) found on the path.
 */
public class AvoidBrick extends Codelet {

    private MemoryObject closestBrickMO;
    private MemoryObject closeBrickFoundMO;
    private MemoryObject innerSenseMO;
    private int reachDistance;
    private MemoryObject legsMO;
    Thing closestBrick;
    Thing leafletJewel;
    Thing closestFood;
    CreatureInnerSense cis;

    public AvoidBrick(int reachDistance) {
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        closestBrickMO = (MemoryObject) this.getInput("CLOSEST_BRICK");
        closeBrickFoundMO = (MemoryObject) this.getOutput("CLOSE_BRICK_FOUND");
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        legsMO = (MemoryObject) this.getOutput("LEGS");
    }

    @Override
    public void proc() {
        String brickName = "";
        closestBrick = (Thing) closestBrickMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();

        if (closestBrick == null) {
            // Nothing to do here.
            return;
        }
        // Check distance to closest brick
        double brickX = 0;
        double brickY = 0;
        try {
            brickX = closestBrick.getX1();
            brickY = closestBrick.getY1();
            brickName = closestBrick.getName();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        double selfX = cis.position.getX();
        double selfY = cis.position.getY();

        Point2D pJewel = new Point();
        pJewel.setLocation(brickX, brickY);

        Point2D pSelf = new Point();
        pSelf.setLocation(selfX, selfY);

        double distance = pSelf.distance(pJewel);

        try {
            if (distance < reachDistance) { // we are too close to a brick.

                JSONObject message = new JSONObject();
                System.out.println("AvoidBrick Stop");
                message.put("ACTION", "GOTO");
                message.put("X", (int) brickX);
                message.put("Y", (int) brickY);
                message.put("SPEED", 0.0);
                //legsMO.updateI(message.toString());
                // Tell the GoToDestinationCodelet to replan the path.
                Boolean closeBrickFound = true;
                closeBrickFoundMO.setI(closeBrickFound);
            } else {
                //legsMO.updateI("");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void calculateActivation() {
    }
}
