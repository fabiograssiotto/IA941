package codelets.behaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.List;
import memory.CreatureInnerSense;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Leaflet;

public class DeliverLeaflets extends Codelet {

    private MemoryObject innerSenseMO;
    private MemoryObject leafletsDoneMO;
    private MemoryObject atDeliverySpotMO;
    private MemoryObject handsMO;

    public DeliverLeaflets() {
    }

    @Override
    public void accessMemoryObjects() {
        innerSenseMO = (MemoryObject) this.getInput("INNER");
        leafletsDoneMO = (MemoryObject) this.getInput("LEAFLETS_DONE");
        atDeliverySpotMO = (MemoryObject) this.getInput("AT_DELIVERYSPOT");
        handsMO = (MemoryObject) this.getOutput("HANDS");
    }

    @Override
    public void proc() {
        Boolean leafletsDone = (Boolean) leafletsDoneMO.getI();
        Boolean atDeliverySpot = (Boolean) atDeliverySpotMO.getI();

        if (leafletsDone == true && atDeliverySpot == true) {
            // deliver leaflets

            JSONObject message = new JSONObject();
            try {
                System.out.println("DeliverLeaflets");

                CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
                List<Leaflet> leaflets = cis.leaflets;

                message.put("ACTION", "DELIVER");
                int leafletNum = 1;
                for (Leaflet l : leaflets) {
                    message.put("LEAFLET" + Integer.toString(leafletNum), Long.toString(l.getID()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Reset leaflets to avoid delivering multiple times.
            Boolean reset = false;
            leafletsDoneMO.setI(reset);
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }

}
