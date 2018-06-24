package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

/**
 * Detects the closest brick in the vision field.
 *
 * @author fabiogr
 *
 */
public class ClosestBrickDetector extends Codelet {

    private MemoryObject visionMO;
    private MemoryObject innerSenseMO;
    private MemoryObject closestBrickMO;

    public ClosestBrickDetector() {
    }

    @Override
    public void accessMemoryObjects() {
        this.visionMO = (MemoryObject) this.getInput("VISION");
        this.innerSenseMO = (MemoryObject) this.getInput("INNER");
        this.closestBrickMO = (MemoryObject) this.getOutput("CLOSEST_BRICK");
    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        Thing closestBrick;
        synchronized (visionMO) {

            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            closestBrick = (Thing) closestBrickMO.getI();
            final CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();

            synchronized (vision) {
                if (vision.isEmpty()) {
                    closestBrick = null;
                } else {
                    // Order vision array based on the distance to the creature.
                    Collections.sort(vision, new Comparator<Thing>() {
                        @Override
                        public int compare(Thing o1, Thing o2) {
                            double dist1 = calculateDistance(o1.getX1(), o1.getY1(), cis.position.getX(), cis.position.getY());
                            double dist2 = calculateDistance(o2.getX1(), o2.getY1(), cis.position.getX(), cis.position.getY());
                            if (dist1 < dist2) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });
                    // Vision array is now sorted, store the closest brick.
                    closestBrick = vision.get(0);
                }
            }
            if (closestBrick != null) {
                closestBrickMO.setI(closestBrick);
            } else {
                closestBrickMO.setI(null);
            }

        }
    }// end proc

    @Override
    public void calculateActivation() {

    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

}//end class

