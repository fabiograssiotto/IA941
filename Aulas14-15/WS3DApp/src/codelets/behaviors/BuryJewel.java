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

public class BuryJewel extends Codelet {

    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;
    private MemoryObject knownMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestJewel;
    CreatureInnerSense cis;
    List<Thing> known;

    public BuryJewel(int reachDistance) {
        setTimeStep(50);
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        closestJewelMO = (MemoryObject) this.getInput("CLOSEST_JEWEL");
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        handsMO = (MemoryObject) this.getOutput("HANDS");
        knownMO = (MemoryObject) this.getOutput("KNOWN_JEWELS");
    }

    @Override
    public void proc() {
        String jewelName = "";
        closestJewel = (Thing) closestJewelMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();
        known = (List<Thing>) knownMO.getI();
        //Find distance between closest apple and self
        //If closer than reachDistance, eat the apple

        if (closestJewel != null) {
            double jewelX = 0;
            double jewelY = 0;
            try {
                jewelX = closestJewel.getX1();
                jewelY = closestJewel.getY1();
                jewelName = closestJewel.getName();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            double selfX = cis.position.getX();
            double selfY = cis.position.getY();

            Point2D pJewel = new Point();
            pJewel.setLocation(jewelX, jewelY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pJewel);
            JSONObject message = new JSONObject();
            try {
                if (distance < reachDistance) { //eat it
                    message.put("OBJECT", jewelName);
                    message.put("ACTION", "BURY");
                    handsMO.updateI(message.toString());
                    DestroyClosestJewel();
                } else {
                    handsMO.updateI("");	//nothing
                }

//				System.out.println(message);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            handsMO.updateI("");	//nothing
        }
        //System.out.println("Before: "+known.size()+ " "+known);

        //System.out.println("After: "+known.size()+ " "+known);
        //System.out.println("EatClosestApple: "+ handsMO.getInfo());
    }

    @Override
    public void calculateActivation() {

    }

    public void DestroyClosestJewel() {
        int r = -1;
        int i = 0;
        synchronized (known) {
            CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
            for (Thing t : known) {
                if (closestJewel != null) {
                    if (t.getName().equals(closestJewel.getName())) {
                        r = i;
                    }
                }
                i++;
            }
            if (r != -1) {
                known.remove(r);
            }
            closestJewel = null;
        }
    }

}
