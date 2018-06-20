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

public class GoToClosestFood extends Codelet {

    private MemoryObject closestFoodMO;
    private MemoryObject selfInfoMO;
    private MemoryContainer legsDecisionMC;
    private int memoryContainerIdx = -1;
    private int creatureBasicSpeed;
    private double reachDistance;

    public GoToClosestFood(int creatureBasicSpeed, int reachDistance) {
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        closestFoodMO = (MemoryObject) this.getInput("CLOSEST_FOOD");
        selfInfoMO = (MemoryObject) this.getInput("INNER");

        // Memory Container for decision
        legsDecisionMC = (MemoryContainer) this.getOutput("LEGS_DECISION_MC");
    }

    @Override
    public void proc() {

        Thing closestFood = (Thing) closestFoodMO.getI();
        CreatureInnerSense cis = (CreatureInnerSense) selfInfoMO.getI();
        double eval = 0.0;

        // Get current fuel state to set as evaluation for the memory container.
        if (cis.fuel < 400) {
            eval = 1.0;
        }

        // Find distance between creature and closest apple
        // If far, go towards it
        // If close, stops
        if (closestFood != null) {
            double foodX = 0;
            double foodY = 0;
            try {
                foodX = closestFood.getX1();
                foodY = closestFood.getY1();

            } catch (Exception e) {
                e.printStackTrace();
            }

            double selfX = cis.position.getX();
            double selfY = cis.position.getY();

            Point2D pFood = new Point();
            pFood.setLocation(foodX, foodY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pFood);
            JSONObject message = new JSONObject();
            try {
                if (distance > reachDistance) { //Go to it
                    System.out.println("GoToClosestFood Go eval = " + eval);
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) foodX);
                    message.put("Y", (int) foodY);
                    message.put("SPEED", creatureBasicSpeed);

                } else {//Stop
                    System.out.println("GoToClosestFood Stop");
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) foodX);
                    message.put("Y", (int) foodY);
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
