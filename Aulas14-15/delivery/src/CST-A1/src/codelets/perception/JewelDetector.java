package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Thing;

/**
 * Detect jewels in the vision field.
 *
 * @author fabiogr
 *
 */
public class JewelDetector extends Codelet {

    private MemoryObject visionMO;
    private MemoryObject knownJewelsMO;

    public JewelDetector() {

    }

    @Override
    public void accessMemoryObjects() {
        synchronized (this) {
            this.visionMO = (MemoryObject) this.getInput("VISION");
        }
        this.knownJewelsMO = (MemoryObject) this.getOutput("KNOWN_JEWELS");
    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Thing> known;
        synchronized (visionMO) {
            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            known = Collections.synchronizedList((List<Thing>) knownJewelsMO.getI());
            synchronized (vision) {
                for (Thing t : vision) {
                    boolean found = false;
                    synchronized (known) {
                        CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                        for (Thing e : myknown) {
                            if (t.getName().equals(e.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (found == false && t.getName().contains("Jewel")) {
                            known.add(t);
                        }
                    }

                }
            }
        }
    }// end proc

    @Override
    public void calculateActivation() {

    }

}//end class

