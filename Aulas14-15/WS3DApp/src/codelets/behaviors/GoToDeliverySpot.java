package codelets.behaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.World;
import ws3dproxy.model.WorldPoint;

public class GoToDeliverySpot extends Codelet {

    private MemoryObject leafletsDoneMO;
    private MemoryContainer legsDecisionMC;

    public GoToDeliverySpot() {
    }

    @Override
    public void accessMemoryObjects() {
        leafletsDoneMO = (MemoryObject) this.getInput("LEAFLETS_DONE");
    }

    @Override
    public void proc() {
        Boolean leafletsDone = (Boolean) leafletsDoneMO.getI();

        if (leafletsDone == true) {
            // Go to delivery spot
            double eval = 1.0;
            JSONObject message = new JSONObject();
            try {
                System.out.println("GoToDeliverySpot GOTO");
                WorldPoint delivery = World.getInstance().getDeliverySpot();
                message.put("ACTION", "GOTO");
                message.put("X", (int) delivery.getX());
                message.put("Y", (int) delivery.getY());
                message.put("SPEED", 3.0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }

}
