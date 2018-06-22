
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
import codelets.behaviors.DeliverLeaflets;
import codelets.behaviors.EatClosestFood;
import codelets.behaviors.Wander;
import codelets.behaviors.GoToClosestFood;
import codelets.behaviors.GoToDeliverySpot;
import codelets.behaviors.GoToLeafletJewel;
import codelets.behaviors.PickupLeafletJewel;
import codelets.behaviors.RemoveObstacle;
import codelets.motor.HandsActionCodelet;
import codelets.motor.LegsActionCodelet;
import codelets.perception.AtDeliverySpotDetector;
import codelets.perception.FoodDetector;
import codelets.perception.ClosestFoodDetector;
import codelets.perception.LeafletJewelDetector;
import codelets.perception.ClosestObstacleDetector;
import codelets.perception.JewelDetector;
import codelets.perception.LeafletsDoneDetector;
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
    private static int obstacleDistance = 80;

    public AgentMind(Environment env) {
        super();

        // Declare Memory Objects
        MemoryObject legsMO;
        MemoryObject handsMO;
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject closestFoodMO;
        MemoryObject knownFoodsMO;
        MemoryObject leafletJewelMO;
        MemoryObject knownJewelsMO;
        MemoryObject closestObstacleMO;
        MemoryObject leafletsDoneMO;
        MemoryObject atDeliverySpotMO;

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
        Thing leafletJewel = null;
        leafletJewelMO = createMemoryObject("LEAFLET_JEWEL", leafletJewel);
        List<Thing> knownJewels = Collections.synchronizedList(new ArrayList<Thing>());
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", knownJewels);
        Thing closestObstacle = null;
        closestObstacleMO = createMemoryObject("CLOSEST_OBSTACLE", closestObstacle);
        Boolean leafletsDone = false;
        leafletsDoneMO = createMemoryObject("LEAFLETS_DONE", leafletsDone);
        Boolean atDeliverySpot = false;
        atDeliverySpotMO = createMemoryObject("AT_DELIVERYSPOT", atDeliverySpot);

        // Initialize Memory Container
        legsDecisionMC = createMemoryContainer("LEGS_DECISION_MC");

        // Create and Populate MindViewer
        MindView mv = new MindView("MindView");
        mv.addMO(knownFoodsMO);
        mv.addMO(knownJewelsMO);
        mv.addMO(visionMO);
        mv.addMO(closestFoodMO);
        mv.addMO(leafletJewelMO);
        mv.addMO(innerSenseMO);
        mv.addMO(handsMO);
        mv.addMO(legsMO);
        mv.addMO(closestObstacleMO);
        mv.addMO(leafletsDoneMO);
        mv.addMO(atDeliverySpotMO);
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

        Codelet hands = new HandsActionCodelet(env.c, this);
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

        Codelet leafletJewelDetector = new LeafletJewelDetector();
        leafletJewelDetector.addInput(knownJewelsMO);
        leafletJewelDetector.addInput(innerSenseMO);
        leafletJewelDetector.addOutput(leafletJewelMO);
        insertCodelet(leafletJewelDetector);

        Codelet closestObstacleDetector = new ClosestObstacleDetector();
        closestObstacleDetector.addInput(visionMO);
        closestObstacleDetector.addInput(innerSenseMO);
        closestObstacleDetector.addOutput(closestObstacleMO);
        insertCodelet(closestObstacleDetector);

        Codelet leafletsDoneDetector = new LeafletsDoneDetector();
        leafletsDoneDetector.addInput(innerSenseMO);
        leafletsDoneDetector.addOutput(leafletsDoneMO);
        insertCodelet(leafletsDoneDetector);

        Codelet atDeliverySpotDetector = new AtDeliverySpotDetector();
        atDeliverySpotDetector.addInput(innerSenseMO);
        atDeliverySpotDetector.addOutput(atDeliverySpotMO);
        insertCodelet(atDeliverySpotDetector);

        // Create Behavior Codelets
        Codelet goToClosestApple = new GoToClosestFood(creatureBasicSpeed, reachDistance);
        goToClosestApple.addInput(closestFoodMO);
        goToClosestApple.addInput(innerSenseMO);
        goToClosestApple.addOutput(legsDecisionMC);
        insertCodelet(goToClosestApple);

        Codelet goToLeafletJewel = new GoToLeafletJewel(creatureBasicSpeed, reachDistance);
        goToLeafletJewel.addInput(leafletJewelMO);
        goToLeafletJewel.addInput(innerSenseMO);
        goToLeafletJewel.addOutput(legsDecisionMC);
        insertCodelet(goToLeafletJewel);

        Codelet goToDeliverySpot = new GoToDeliverySpot(creatureBasicSpeed);
        goToDeliverySpot.addInput(leafletsDoneMO);
        goToDeliverySpot.addOutput(legsDecisionMC);
        insertCodelet(goToDeliverySpot);

        Codelet deliverLeaflets = new DeliverLeaflets();
        deliverLeaflets.addInput(innerSenseMO);
        deliverLeaflets.addInput(leafletsDoneMO);
        deliverLeaflets.addInput(atDeliverySpotMO);
        deliverLeaflets.addOutput(handsMO);
        insertCodelet(deliverLeaflets);

        Codelet eatFood = new EatClosestFood(reachDistance);
        eatFood.addInput(closestFoodMO);
        eatFood.addInput(innerSenseMO);
        eatFood.addOutput(handsMO);
        eatFood.addOutput(knownFoodsMO);
        insertCodelet(eatFood);

        Codelet pickupJewel = new PickupLeafletJewel(reachDistance);
        pickupJewel.addInput(leafletJewelMO);
        pickupJewel.addInput(innerSenseMO);
        pickupJewel.addOutput(handsMO);
        pickupJewel.addOutput(knownJewelsMO);
        insertCodelet(pickupJewel);

        Codelet removeObstacle = new RemoveObstacle(obstacleDistance);
        removeObstacle.addInput(closestObstacleMO);
        removeObstacle.addInput(leafletJewelMO);
        removeObstacle.addInput(closestFoodMO);
        removeObstacle.addInput(innerSenseMO);
        removeObstacle.addOutput(handsMO);
        insertCodelet(removeObstacle);

        Codelet wander = new Wander();
        wander.addInput(innerSenseMO);
        wander.addInput(knownFoodsMO);
        wander.addInput(knownJewelsMO);
        wander.addOutput(legsDecisionMC);
        insertCodelet(wander);

        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets()) {
            c.setTimeStep(50);
        }

        // Start Cognitive Cycle
        start();
    }

    public void shutdown() {
        // Stop all codelets threads.
        shutdown();
    }

}
