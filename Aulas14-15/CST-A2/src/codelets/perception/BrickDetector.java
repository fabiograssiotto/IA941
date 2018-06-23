package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/**
 * Detect jewels in the vision field.
 *
 * @author fabiogr
 *
 */
public class BrickDetector extends Codelet {

    private MemoryObject visionMO;
    private MemoryObject brickListMO;

    public BrickDetector() {

    }

    @Override
    public void accessMemoryObjects() {
        synchronized (this) {
            this.visionMO = (MemoryObject) this.getInput("VISION");
        }
        this.brickListMO = (MemoryObject) this.getOutput("BRICK_LIST");
    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Thing> brickList;
        synchronized (visionMO) {
            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            brickList = Collections.synchronizedList((List<Thing>) brickListMO.getI());
            synchronized (vision) {
                for (Thing t : vision) {
                    boolean found = false;
                    synchronized (brickList) {
                        CopyOnWriteArrayList<Thing> myList = new CopyOnWriteArrayList<>(brickList);
                        for (Thing e : myList) {
                            if (t.getName().equals(e.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (found == false && t.getCategory() == Constants.categoryBRICK) {
                            myList.add(t);
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

