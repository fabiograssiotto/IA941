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
package main;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import codelets.behaviors.AvoidBrick;
import codelets.behaviors.GoToDestination;
import codelets.motor.LegsActionCodelet;
import codelets.perception.BrickDetector;
import codelets.perception.ClosestBrickDetector;
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

    final private static double creatureBasicSpeed = 1.5;
    final private static int reachDistance = 80;

    public AgentMind(Environment env) {
        super();

        // Declare Memory Objects
        MemoryObject legsMO;
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject brickListMO;
        MemoryObject closestBrickMO;
        MemoryObject closeBrickFoundMO;

        //Initialize Memory Objects
        legsMO = createMemoryObject("LEGS", "");
        List<Thing> vision_list = Collections.synchronizedList(new ArrayList<Thing>());
        visionMO = createMemoryObject("VISION", vision_list);
        CreatureInnerSense cis = new CreatureInnerSense();
        innerSenseMO = createMemoryObject("INNER", cis);
        List<Thing> brickList = Collections.synchronizedList(new ArrayList<Thing>());
        brickListMO = createMemoryObject("BRICK_LIST", brickList);
        Thing closestBrick = null;
        closestBrickMO = createMemoryObject("CLOSEST_BRICK", closestBrick);
        Boolean closeBrickFound = false;
        closeBrickFoundMO = createMemoryObject("CLOSE_BRICK_FOUND", closeBrickFound);

        // Create and Populate MindViewer
        MindView mv = new MindView("MindView");
        mv.addMO(visionMO);
        mv.addMO(innerSenseMO);
        mv.addMO(legsMO);
        mv.addMO(brickListMO);
        mv.addMO(closestBrickMO);
        mv.addMO(closeBrickFoundMO);
        mv.StartTimer();
        mv.setVisible(true);

        // Create Sensor Codelets
        Codelet vision = new Vision(env.c);
        vision.addOutput(visionMO);
        insertCodelet(vision); // Creates a vision sensor

        Codelet innerSense = new InnerSense(env.c);
        innerSense.addOutput(innerSenseMO);
        insertCodelet(innerSense); // A sensor for the inner state of the creature

        // Create Actuator Codelets
        Codelet legs = new LegsActionCodelet(env.c);
        legs.addInput(legsMO);
        insertCodelet(legs);

        // Create Perception Codelets
        Codelet brickDetector = new BrickDetector();
        brickDetector.addInput(visionMO);
        brickDetector.addOutput(brickListMO);
        insertCodelet(brickDetector);

        Codelet closestBrickDetector = new ClosestBrickDetector();
        closestBrickDetector.addInput(visionMO);
        closestBrickDetector.addInput(innerSenseMO);
        closestBrickDetector.addOutput(closestBrickMO);
        insertCodelet(closestBrickDetector);

        // Create Behavior Codelets
        Codelet goToDestination = new GoToDestination(creatureBasicSpeed, this);
        goToDestination.addInput(brickListMO);
        goToDestination.addInput(innerSenseMO);
        goToDestination.addOutput(legsMO);
        goToDestination.addOutput(closeBrickFoundMO);
        insertCodelet(goToDestination);

        Codelet avoidBrick = new AvoidBrick(reachDistance);
        avoidBrick.addInput(closestBrickMO);
        avoidBrick.addInput(innerSenseMO);
        avoidBrick.addOutput(closeBrickFoundMO);
        avoidBrick.addOutput(legsMO);
        insertCodelet(avoidBrick);

        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets()) {
            String codeletClass = c.getClass().toString();
            String goToDestinationClass = goToDestination.getClass().toString();
            String legsClass = legs.getClass().toString();
            String avoidBrickClass = avoidBrick.getClass().toString();
            if (codeletClass.equals(legsClass)
                    || codeletClass.equals(goToDestinationClass)
                    || codeletClass.equals(avoidBrickClass)) {
                c.setTimeStep(20);
            } else {
                c.setTimeStep(200);
            }
        }

        // Start Cognitive Cycle
        start();
    }

    public void shutdown() {
        // Stop all codelets threads.
        shutdown();
    }

}
