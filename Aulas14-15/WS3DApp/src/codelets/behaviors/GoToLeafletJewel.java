package codelets.behaviors;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

public class GoToLeafletJewel extends Codelet {

    private MemoryObject leafletJewelMO;
    private MemoryObject selfInfoMO;
    private MemoryContainer legsDecisionMC;
    private int memoryContainerIdx = -1;
    final private int creatureBasicSpeed;
    final private double reachDistance;

    public GoToLeafletJewel(int creatureBasicSpeed, int reachDistance) {
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        leafletJewelMO = (MemoryObject) this.getInput("LEAFLET_JEWEL");
        selfInfoMO = (MemoryObject) this.getInput("INNER");

        // Memory Container for decision
        legsDecisionMC = (MemoryContainer) this.getOutput("LEGS_DECISION_MC");
    }

    @Override
    public void proc() {
        // Find distance between creature and closest apple
        //If far, go towards it
        //If close, stops

        Thing leafletJewel = (Thing) leafletJewelMO.getI();
        CreatureInnerSense cis = (CreatureInnerSense) selfInfoMO.getI();
        double eval = 0;
        JSONObject message = new JSONObject();

        if (leafletJewel != null) {
            eval = 0.5;
            double jewelX = 0;
            double jewelY = 0;
            try {
                jewelX = leafletJewel.getX1();
                jewelY = leafletJewel.getY1();

            } catch (Exception e) {
                e.printStackTrace();
            }

            double selfX = cis.position.getX();
            double selfY = cis.position.getY();

            Point2D pJewel = new Point();
            pJewel.setLocation(jewelX, jewelY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pJewel);

            try {
                if (distance > reachDistance) { //Go to it
                    System.out.println("GoToLeafletJewel Go");
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) jewelX);
                    message.put("Y", (int) jewelY);
                    message.put("SPEED", creatureBasicSpeed);

                } else {//Stop
                    System.out.println("GoToLeafletJewel Stop");
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) jewelX);
                    message.put("Y", (int) jewelY);
                    message.put("SPEED", 0.0);
                }

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
