package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import memory.CreatureInnerSense;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Thing;

/**
 * @author fabiogr
 *
 */
public class ClosestFoodDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject closestFoodMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;

    public ClosestFoodDetector() {
    }

    @Override
    public void accessMemoryObjects() {
        this.knownMO = (MemoryObject) this.getInput("KNOWN_FOODS");
        this.innerSenseMO = (MemoryObject) this.getInput("INNER");
        this.closestFoodMO = (MemoryObject) this.getOutput("CLOSEST_FOOD");
    }

    @Override
    public void proc() {
        Thing closest_food = null;
        known = Collections.synchronizedList((List<Thing>) knownMO.getI());
        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
        synchronized (known) {
            if (known.size() != 0) {
                //Iterate over objects in vision, looking for the closest apple
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    if (objectName.contains("PFood") && !objectName.contains("NPFood")) { //These are foods
                        if (closest_food == null) {
                            closest_food = t;
                        } else {
                            double Dnew = calculateDistance(t.getX1(), t.getY1(), cis.position.getX(), cis.position.getY());
                            double Dclosest = calculateDistance(closest_food.getX1(), closest_food.getY1(), cis.position.getX(), cis.position.getY());
                            if (Dnew < Dclosest) {
                                closest_food = t;
                            }
                        }
                    }
                }

                if (closest_food != null) {
                    if (closestFoodMO.getI() == null || !closestFoodMO.getI().equals(closest_food)) {
                        closestFoodMO.setI(closest_food);
                    }

                } else {
                    //couldn't find any nearby apples
                    closest_food = null;
                    closestFoodMO.setI(closest_food);
                }
            } else { // if there are no known apples closest_apple must be null
                closest_food = null;
                closestFoodMO.setI(closest_food);
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
