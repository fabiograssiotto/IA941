package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.HashMap;
import memory.CreatureInnerSense;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;

/**
 * @author fabiogr This codelet identifies the closest jewel that is also required for one of the leaflets.
 */
public class LeafletJewelDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject LeafletJewelMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;

    public LeafletJewelDetector() {
    }

    @Override
    public void accessMemoryObjects() {
        this.knownMO = (MemoryObject) this.getInput("KNOWN_JEWELS");
        this.innerSenseMO = (MemoryObject) this.getInput("INNER");
        this.LeafletJewelMO = (MemoryObject) this.getOutput("LEAFLET_JEWEL");
    }

    @Override
    public void proc() {
        Thing leafletJewel = null;
        known = Collections.synchronizedList((List<Thing>) knownMO.getI());
        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
        synchronized (known) {
            if (!known.isEmpty()) {
                //Iterate over objects in vision, looking for the jewels in the leaflet
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    String objectColor = t.getAttributes().getColor();

                    if (objectName.contains("Jewel")) { // it is a jewel
                        // Check if the jewel color is required for any of the leaflets.
                        List<Leaflet> leaflets = cis.leaflets;
                        boolean jewelFound = false;
                        for (Leaflet l : leaflets) {
                            HashMap<String, Integer[]> items = l.getItems();

                            // Are we still missing this jewel color in this leaflet?
                            if (l.getMissingNumberOfType(objectColor) > 0) {
                                jewelFound = true;
                                break;
                            }
                        }
                        if (!jewelFound) {
                            // Go to the next jewel, as this one is not required for any of the leaflets.
                            continue;
                        } else {
                            // Jewel color found in one of the leaflets.
                            if (leafletJewel == null) {
                                leafletJewel = t;
                            } else {
                                double Dnew = calculateDistance(t.getX1(), t.getY1(), cis.position.getX(), cis.position.getY());
                                double Dclosest = calculateDistance(leafletJewel.getX1(), leafletJewel.getY1(), cis.position.getX(), cis.position.getY());
                                if (Dnew < Dclosest) {
                                    leafletJewel = t;
                                }
                            }
                        }
                    }
                }

                if (leafletJewel != null) {
                    if (LeafletJewelMO.getI() == null || !LeafletJewelMO.getI().equals(leafletJewel)) {
                        LeafletJewelMO.setI(leafletJewel);
                    }

                } else {
                    //couldn't find any nearby jewels that are on the leaflets
                    leafletJewel = null;
                    LeafletJewelMO.setI(leafletJewel);
                }
            } else { // if there are no known jewels closest_jewel must be null
                leafletJewel = null;
                LeafletJewelMO.setI(leafletJewel);
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
