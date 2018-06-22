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
    private int memoryContainerIdx = -1;
    private int creatureBasicSpeed;

    public GoToDeliverySpot(int creatureBasicSpeed) {
        this.creatureBasicSpeed = creatureBasicSpeed;
    }

    @Override
    public void accessMemoryObjects() {
        leafletsDoneMO = (MemoryObject) this.getInput("LEAFLETS_DONE");
        // Memory Container for decision
        legsDecisionMC = (MemoryContainer) this.getOutput("LEGS_DECISION_MC");
    }

    @Override
    public void proc() {

        Boolean leafletsDone = (Boolean) leafletsDoneMO.getI();
        double eval = 0;
        JSONObject message = new JSONObject();

        if (leafletsDone == true) {
            // Go to delivery spot
            eval = 1.0;

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
        if (memoryContainerIdx == -1) {
            memoryContainerIdx = legsDecisionMC.setI(message.toString(), eval);
        } else {
            legsDecisionMC.setI(message, eval, memoryContainerIdx);
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }

}
