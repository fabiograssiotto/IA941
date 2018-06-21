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
 * @author fabiogr This codelet identifies that all creature leaflet jewels have been collected.
 */
public class LeafletsDoneDetector extends Codelet {

    private MemoryObject innerSenseMO;
    private MemoryObject leafletsDoneMO;

    private List<Thing> known;

    public LeafletsDoneDetector() {
    }

    @Override
    public void accessMemoryObjects() {
        this.innerSenseMO = (MemoryObject) this.getInput("INNER");
        this.leafletsDoneMO = (MemoryObject) this.getOutput("LEAFLETS_DONE");
    }

    @Override
    public void proc() {
        CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
        List<Leaflet> leaflets = cis.leaflets;

        boolean leafletsDone = false;
        for (Leaflet l : leaflets) {
            // Check if we are still missing any jewels on the leaflets.
            if ((l.getMissingNumberOfType("Red") <= 0)
                    && (l.getMissingNumberOfType("Green") <= 0)
                    && (l.getMissingNumberOfType("Blue") <= 0)
                    && (l.getMissingNumberOfType("Yellow") <= 0)
                    && (l.getMissingNumberOfType("Magenta") <= 0)
                    && (l.getMissingNumberOfType("White") <= 0)) {
                // This leaflet is done.
                leafletsDone = true;
            } else {
                leafletsDone = false;
                break;
            }
        }
        leafletsDoneMO.setI(leafletsDone);
    }//end proc

    @Override
    public void calculateActivation() {

    }
}
