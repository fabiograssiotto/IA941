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

public class PickupLeafletJewel extends Codelet {

    private MemoryObject leafletJewelMO;
    private MemoryObject innerSenseMO;
    private MemoryObject knownMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing leafletJewel;
    CreatureInnerSense cis;
    List<Thing> known;

    public PickupLeafletJewel(int reachDistance) {
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        leafletJewelMO = (MemoryObject) this.getInput("LEAFLET_JEWEL");
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        handsMO = (MemoryObject) this.getOutput("HANDS");
        knownMO = (MemoryObject) this.getOutput("KNOWN_JEWELS");
    }

    @Override
    public void proc() {
        String jewelName = "";
        leafletJewel = (Thing) leafletJewelMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();
        known = (List<Thing>) knownMO.getI();
        //Find distance between the leaflet jewel and self
        //If closer than reachDistance, pick up the jewel

        if (leafletJewel != null) {
            double jewelX = 0;
            double jewelY = 0;
            try {
                jewelX = leafletJewel.getX1();
                jewelY = leafletJewel.getY1();
                jewelName = leafletJewel.getName();

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
                if (distance < reachDistance) { // pickup the jewel
                    System.out.println("PickupLeafletJewel Pickup");
                    message.put("OBJECT", jewelName);
                    message.put("ACTION", "PICKUP");
                    handsMO.updateI(message.toString());
                    DestroyLeafletJewel();
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
    }

    @Override
    public void calculateActivation() {

    }

    public void DestroyLeafletJewel() {
        int r = -1;
        int i = 0;
        synchronized (known) {
            CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
            for (Thing t : known) {
                if (leafletJewel != null) {
                    if (t.getName().equals(leafletJewel.getName())) {
                        r = i;
                    }
                }
                i++;
            }
            if (r != -1) {
                known.remove(r);
            }
            leafletJewel = null;
            knownMO.setI(known);
            leafletJewelMO.setI(leafletJewel);
        }
    }

}
