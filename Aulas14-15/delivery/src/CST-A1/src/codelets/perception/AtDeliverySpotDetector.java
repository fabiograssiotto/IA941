package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import ws3dproxy.model.World;
import ws3dproxy.model.WorldPoint;

/**
 * @author fabiogr This codelet identifies that all creature leaflet jewels have been collected.
 */
public class AtDeliverySpotDetector extends Codelet {

    private MemoryObject innerSenseMO;
    private MemoryObject atDeliverySpotMO;
    final private static double DISTANCE = 30.0;

    public AtDeliverySpotDetector() {
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) this.getInput("INNER");
        this.atDeliverySpotMO = (MemoryObject) this.getOutput("AT_DELIVERYSPOT");
    }

    @Override
    public void proc() {
        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();

        boolean atDeliverySpot = false;
        if (cis != null && cis.position != null) {
            double crX = cis.position.getX();
            double crY = cis.position.getY();

            WorldPoint delivery = World.getInstance().getDeliverySpot();
            double delX = delivery.getX();
            double delY = delivery.getY();

            if (calculateDistance(crX, crY, delX, delY) < DISTANCE) {
                atDeliverySpot = true;
            }
        }

        atDeliverySpotMO.setI(atDeliverySpot);
    }//end proc

    @Override
    public void calculateActivation() {

    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }
}
