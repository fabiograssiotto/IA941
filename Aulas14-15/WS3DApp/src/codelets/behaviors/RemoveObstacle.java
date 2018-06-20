package codelets.behaviors;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/*
* Removes when necessary objects that were not the target of the creature.
* Basically this is for objects (food/jewels) found on the path.
 */
public class RemoveObstacle extends Codelet {

    private MemoryObject closestObstacleMO;
    private MemoryObject closestJewelMO;
    private MemoryObject closestFoodMO;
    private MemoryObject innerSenseMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestObstacle;
    Thing closestJewel;
    Thing closestFood;
    CreatureInnerSense cis;

    public RemoveObstacle(int reachDistance) {
        setTimeStep(50);
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        closestObstacleMO = (MemoryObject) this.getInput("CLOSEST_OBSTACLE");
        closestJewelMO = (MemoryObject) this.getInput("CLOSEST_JEWEL");
        closestFoodMO = (MemoryObject) this.getInput("CLOSEST_FOOD");
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        handsMO = (MemoryObject) this.getOutput("HANDS");
    }

    @Override
    public void proc() {
        String obstacleName = "";
        closestObstacle = (Thing) closestObstacleMO.getI();
        closestJewel = (Thing) closestJewelMO.getI();
        closestFood = (Thing) closestFoodMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();

        // Check if the closest thing is not the closest jewel.
        // In case it is not, it is an obstacle, so get rid of it.
        if (closestObstacle != null
                && ((closestObstacle.getCategory() == Constants.categoryJEWEL && closestJewel != null && !closestObstacle.getName().equals(closestJewel.getName()))
                || ((closestObstacle.getCategory() == Constants.categoryPFOOD || closestObstacle.getCategory() == Constants.categoryNPFOOD)
                && closestFood != null && !closestObstacle.getName().equals(closestFood.getName())))) {

            System.out.println("RemoveObstacle obstacle = " + closestObstacle.getName());

            double obstacleX = 0;
            double obstacleY = 0;
            try {
                obstacleX = closestObstacle.getX1();
                obstacleY = closestObstacle.getY1();
                obstacleName = closestObstacle.getName();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            double selfX = cis.position.getX();
            double selfY = cis.position.getY();

            Point2D pJewel = new Point();
            pJewel.setLocation(obstacleX, obstacleY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pJewel);
            JSONObject message = new JSONObject();
            try {
                if (distance < reachDistance) { // bury it, as we do not need it.
                    System.out.println("RemoveObstacle Bury");
                    message.put("OBJECT", obstacleName);
                    message.put("ACTION", "BURY");
                    handsMO.updateI(message.toString());
                    DestroyClosestThing();
                } else {
                    handsMO.updateI("");	//nothing
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            handsMO.updateI("");	//nothing
        }

    }

    @Override
    public void calculateActivation() {
    }

    public void DestroyClosestThing() {
        closestObstacle = null;
    }
}
