package codelets.behaviors;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/*
* Removes when necessary objects that were not the target of the creature.
* Basically this is for objects (food/jewels) found on the path.
 */
public class RemoveObstacle extends Codelet {

    private MemoryObject closestObstacleMO;
    private MemoryObject leafletJewelMO;
    private MemoryObject closestFoodMO;
    private MemoryObject innerSenseMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestObstacle;
    Thing leafletJewel;
    Thing closestFood;
    CreatureInnerSense cis;

    public RemoveObstacle(int reachDistance) {
        setTimeStep(50);
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        closestObstacleMO = (MemoryObject) this.getInput("CLOSEST_OBSTACLE");
        leafletJewelMO = (MemoryObject) this.getInput("LEAFLET_JEWEL");
        closestFoodMO = (MemoryObject) this.getInput("CLOSEST_FOOD");
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        handsMO = (MemoryObject) this.getOutput("HANDS");
    }

    @Override
    public void proc() {
        String obstacleName = "";
        closestObstacle = (Thing) closestObstacleMO.getI();
        leafletJewel = (Thing) leafletJewelMO.getI();
        closestFood = (Thing) closestFoodMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();

        // Check all the conditions to remove an obstacle from the environment.
        Boolean canRemove = false;

        if (closestObstacle != null && isJewel(closestObstacle) && leafletJewel == null) {
            // That is, no jewel to collect but we found a jewel
            canRemove = true;
        } else if (closestObstacle != null && isFood(closestObstacle) && closestFood == null) {
            // That is, no food to collect but we found some food
            canRemove = true;
        } else if (closestObstacle != null && isJewel(closestObstacle) && leafletJewel != null && !closestObstacle.getName().equals(leafletJewel.getName())) {
            // That is, the obstacle is not the current jewel we are looking for.
            canRemove = true;
        } else if (closestObstacle != null && isFood(closestObstacle) && closestFood != null && !closestObstacle.getName().equals(closestFood.getName())) {
            // That is, the obstacle is not the current food we are looking for.
            canRemove = true;
        }

        if (canRemove) {

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
                    if (isJewel(closestObstacle)) {
                        System.out.println("RemoveObstacle BURY");
                        message.put("OBJECT", obstacleName);
                        message.put("ACTION", "BURY");
                        handsMO.updateI(message.toString());

                    } else {
                        System.out.println("RemoveObstacle EATIT");
                        message.put("OBJECT", obstacleName);
                        message.put("ACTION", "EATIT");
                        handsMO.updateI(message.toString());
                    }
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

    private Boolean isFood(Thing t) {
        if (t.getCategory() == Constants.categoryPFOOD || t.getCategory() == Constants.categoryNPFOOD) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean isJewel(Thing t) {
        if (t.getCategory() == Constants.categoryJEWEL) {
            return true;
        } else {
            return false;
        }
    }
}
