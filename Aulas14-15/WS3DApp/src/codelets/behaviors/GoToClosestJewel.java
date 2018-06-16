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

public class GoToClosestJewel extends Codelet {

    private MemoryObject closestJewelMO;
    private MemoryObject selfInfoMO;
    private MemoryContainer legsDecisionMC;
    private int memoryContainerIdx = -1;
    final private int creatureBasicSpeed;
    final private double reachDistance;

    public GoToClosestJewel(int creatureBasicSpeed, int reachDistance) {
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        closestJewelMO = (MemoryObject) this.getInput("CLOSEST_JEWEL");
        selfInfoMO = (MemoryObject) this.getInput("INNER");

        // Memory Container for decision
        legsDecisionMC = (MemoryContainer) this.getOutput("LEGS_DECISION_MC");
    }

    @Override
    public void proc() {
        // Find distance between creature and closest apple
        //If far, go towards it
        //If close, stops

        Thing closestJewel = (Thing) closestJewelMO.getI();
        CreatureInnerSense cis = (CreatureInnerSense) selfInfoMO.getI();
        double eval = 0.5; // constant evaluator

        if (closestJewel != null) {
            double jewelX = 0;
            double jewelY = 0;
            try {
                jewelX = closestJewel.getX1();
                jewelY = closestJewel.getY1();

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
            JSONObject message = new JSONObject();
            try {
                if (distance > reachDistance) { //Go to it
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) jewelX);
                    message.put("Y", (int) jewelY);
                    message.put("SPEED", creatureBasicSpeed);

                } else {//Stop
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) jewelX);
                    message.put("Y", (int) jewelY);
                    message.put("SPEED", 0.0);
                }
                if (memoryContainerIdx == -1) {
                    memoryContainerIdx = legsDecisionMC.setI(message.toString(), eval);
                } else {
                    legsDecisionMC.setI(message, eval, memoryContainerIdx);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }

}