
/**
 * ***************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 ****************************************************************************
 */
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import codelets.behaviors.EatClosestFood;
import codelets.behaviors.Wander;
import codelets.behaviors.GoToClosestFood;
import codelets.behaviors.GoToClosestJewel;
import codelets.behaviors.PickupClosestJewel;
import codelets.behaviors.RemoveObstacle;
import codelets.motor.HandsActionCodelet;
import codelets.motor.LegsActionCodelet;
import codelets.perception.FoodDetector;
import codelets.perception.ClosestFoodDetector;
import codelets.perception.ClosestJewelDetector;
import codelets.perception.ClosestObstacleDetector;
import codelets.perception.JewelDetector;
import codelets.sensors.InnerSense;
import codelets.sensors.Vision;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import memory.CreatureInnerSense;
import support.MindView;
import ws3dproxy.model.Thing;

/**
 *
 * @author rgudwin
 */
public class AgentMind extends Mind {

    private static int creatureBasicSpeed = 3;
    private static int reachDistance = 50;

    public AgentMind(Environment env) {
        super();

        // Declare Memory Objects
        MemoryObject legsMO;
        MemoryObject handsMO;
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject closestFoodMO;
        MemoryObject knownFoodsMO;
        MemoryObject closestJewelMO;
        MemoryObject knownJewelsMO;
        MemoryObject closestObstacleMO;

        // Declare Memory Container for Legs Decision
        MemoryContainer legsDecisionMC;

        //Initialize Memory Objects
        legsMO = createMemoryObject("LEGS", "");
        handsMO = createMemoryObject("HANDS", "");
        List<Thing> vision_list = Collections.synchronizedList(new ArrayList<Thing>());
        visionMO = createMemoryObject("VISION", vision_list);
        CreatureInnerSense cis = new CreatureInnerSense();
        innerSenseMO = createMemoryObject("INNER", cis);
        Thing closestFood = null;
        closestFoodMO = createMemoryObject("CLOSEST_FOOD", closestFood);
        List<Thing> knownFoods = Collections.synchronizedList(new ArrayList<Thing>());
        knownFoodsMO = createMemoryObject("KNOWN_FOODS", knownFoods);
        Thing closestJewel = null;
        closestJewelMO = createMemoryObject("CLOSEST_JEWEL", closestJewel);
        List<Thing> knownJewels = Collections.synchronizedList(new ArrayList<Thing>());
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", knownJewels);
        Thing closestObstacle = null;
        closestObstacleMO = createMemoryObject("CLOSEST_OBSTACLE", closestObstacle);

        // Initialize Memory Container
        legsDecisionMC = createMemoryContainer("LEGS_DECISION_MC");

        // Create and Populate MindViewer
        MindView mv = new MindView("MindView");
        mv.addMO(knownFoodsMO);
        mv.addMO(knownJewelsMO);
        mv.addMO(visionMO);
        mv.addMO(closestFoodMO);
        mv.addMO(closestJewelMO);
        mv.addMO(innerSenseMO);
        mv.addMO(handsMO);
        mv.addMO(legsMO);
        mv.addMO(closestObstacleMO);
        mv.StartTimer();
        mv.setVisible(true);

        // Create Sensor Codelets
        Codelet vision = new Vision(env.c);
        vision.addOutput(visionMO);
        insertCodelet(vision); //Creates a vision sensor

        Codelet innerSense = new InnerSense(env.c);
        innerSense.addOutput(innerSenseMO);
        insertCodelet(innerSense); //A sensor for the inner state of the creature

        // Create Actuator Codelets
        Codelet legs = new LegsActionCodelet(env.c);
        legs.addInput(legsDecisionMC);
        insertCodelet(legs);

        Codelet hands = new HandsActionCodelet(env.c);
        hands.addInput(handsMO);
        insertCodelet(hands);

        // Create Perception Codelets
        Codelet fd = new FoodDetector();
        fd.addInput(visionMO);
        fd.addOutput(knownFoodsMO);
        insertCodelet(fd);

        Codelet closestFoodDetector = new ClosestFoodDetector();
        closestFoodDetector.addInput(knownFoodsMO);
        closestFoodDetector.addInput(innerSenseMO);
        closestFoodDetector.addOutput(closestFoodMO);
        insertCodelet(closestFoodDetector);

        Codelet jd = new JewelDetector();
        jd.addInput(visionMO);
        jd.addOutput(knownJewelsMO);
        insertCodelet(jd);

        Codelet closestJewelDetector = new ClosestJewelDetector();
        closestJewelDetector.addInput(knownJewelsMO);
        closestJewelDetector.addInput(innerSenseMO);
        closestJewelDetector.addOutput(closestJewelMO);
        insertCodelet(closestJewelDetector);

        Codelet closestObstacleDetector = new ClosestObstacleDetector();
        closestObstacleDetector.addInput(visionMO);
        closestObstacleDetector.addInput(innerSenseMO);
        closestObstacleDetector.addOutput(closestObstacleMO);
        insertCodelet(closestObstacleDetector);

        // Create Behavior Codelets
        Codelet goToClosestApple = new GoToClosestFood(creatureBasicSpeed, reachDistance);
        goToClosestApple.addInput(closestFoodMO);
        goToClosestApple.addInput(innerSenseMO);
        goToClosestApple.addOutput(legsDecisionMC);
        insertCodelet(goToClosestApple);

        Codelet goToClosestJewel = new GoToClosestJewel(creatureBasicSpeed, reachDistance);
        goToClosestJewel.addInput(closestJewelMO);
        goToClosestJewel.addInput(innerSenseMO);
        goToClosestJewel.addOutput(legsDecisionMC);
        insertCodelet(goToClosestJewel);

        Codelet eatFood = new EatClosestFood(reachDistance);
        eatFood.addInput(closestFoodMO);
        eatFood.addInput(innerSenseMO);
        eatFood.addOutput(handsMO);
        eatFood.addOutput(knownFoodsMO);
        insertCodelet(eatFood);

        Codelet pickupJewel = new PickupClosestJewel(reachDistance);
        pickupJewel.addInput(closestJewelMO);
        pickupJewel.addInput(innerSenseMO);
        pickupJewel.addOutput(handsMO);
        pickupJewel.addOutput(knownJewelsMO);
        insertCodelet(pickupJewel);

        Codelet removeObstacle = new RemoveObstacle(reachDistance);
        removeObstacle.addInput(closestObstacleMO);
        removeObstacle.addInput(closestJewelMO);
        removeObstacle.addInput(innerSenseMO);
        removeObstacle.addOutput(handsMO);
        insertCodelet(removeObstacle);

        Codelet wander = new Wander();
        wander.addInput(innerSenseMO);
        wander.addInput(knownFoodsMO);
        wander.addInput(knownJewelsMO);
        wander.addInput(closestFoodMO);
        wander.addInput(closestJewelMO);
        wander.addOutput(legsDecisionMC);
        insertCodelet(wander);

        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets()) {
            c.setTimeStep(200);
        }

        // Start Cognitive Cycle
        start();
    }

}
