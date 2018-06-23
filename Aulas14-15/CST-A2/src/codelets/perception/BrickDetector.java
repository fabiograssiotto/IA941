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
    private MemoryObject newBrickFoundMO;

    public BrickDetector() {

    }

    @Override
    public void accessMemoryObjects() {
        synchronized (this) {
            this.visionMO = (MemoryObject) this.getInput("VISION");
        }
        this.brickListMO = (MemoryObject) this.getOutput("BRICK_LIST");
        this.newBrickFoundMO = (MemoryObject) this.getOutput("NEWBRICK_FOUND");
    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Thing> brickList;
        Boolean newBrickFound = false;
        synchronized (visionMO) {
            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            brickList = Collections.synchronizedList((List<Thing>) brickListMO.getI());
            CopyOnWriteArrayList<Thing> myList = new CopyOnWriteArrayList<>(brickList);
            synchronized (vision) {
                for (Thing t : vision) {
                    synchronized (brickList) {
                        if (t.getCategory() == Constants.categoryBRICK) {
                            if (myList.contains(t)) {
                                continue;
                            } else {
                                // Add to list
                                myList.add(t);
                                newBrickFound = true;
                            }
                        }
                    }
                }
                brickListMO.setI(myList);
                newBrickFoundMO.setI(newBrickFound);
            }
        }
    }// end proc

    @Override
    public void calculateActivation() {
    }

}//end class

