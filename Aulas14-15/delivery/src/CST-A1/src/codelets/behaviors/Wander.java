package codelets.behaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.List;
import memory.CreatureInnerSense;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Thing;

/**
 *
 * @author fabiogr
 *
 *
 */
public class Wander extends Codelet {

    private MemoryObject knownFoodsMO;
    private MemoryObject knownJewelsMO;
    private MemoryObject selfInfoMO;
    private List<Thing> knownFoods;
    private List<Thing> knownJewels;
    private MemoryContainer legsDecisionMC;
    private int memoryContainerIdx = -1;

    /**
     * Default constructor
     */
    public Wander() {
    }

    @Override
    public void proc() {

        CreatureInnerSense cis = (CreatureInnerSense) selfInfoMO.getI();
        knownFoods = (List<Thing>) knownFoodsMO.getI();
        knownJewels = (List<Thing>) knownJewelsMO.getI();

        if ((knownJewels.size() == 0 || (knownFoods.size() == 0 && cis.fuel < 400))) {
            JSONObject message = new JSONObject();
            try {
                double eval = 0.2;
                System.out.println("Wander eval = " + eval);
                message.put("ACTION", "WANDER");

                if (memoryContainerIdx == -1) {
                    memoryContainerIdx = legsDecisionMC.setI(message.toString(), eval);
                } else {
                    legsDecisionMC.setI(message, eval, memoryContainerIdx);
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public void accessMemoryObjects() {
        knownFoodsMO = (MemoryObject) this.getInput("KNOWN_FOODS");
        knownJewelsMO = (MemoryObject) this.getInput("KNOWN_JEWELS");
        selfInfoMO = (MemoryObject) this.getInput("INNER");
        legsDecisionMC = (MemoryContainer) this.getOutput("LEGS_DECISION_MC");
    }

    @Override
    public void calculateActivation() {

    }

}
