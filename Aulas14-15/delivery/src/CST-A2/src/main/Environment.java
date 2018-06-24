package main;

/**
 * ***************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Contributors: Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin ***************************************************************************
 */
import static java.lang.Math.abs;
import java.util.Random;
import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

/**
 *
 * @author rgudwin
 */
public class Environment {

    public String host = "localhost";
    public int port = 4011;
    public String robotID = "r0";
    public Creature c = null;

    // Coordinates for the creature destination
    public static int destinationX = 500;
    public static int destinationY = 500;

    private static Boolean CREATEBRICKS = true;

    public Environment() {

        WS3DProxy proxy = new WS3DProxy();
        try {
            World w = World.getInstance();
            w.reset();

            int crX = 0;
            int crY = 0;
            c = proxy.createCreature(crX, crY, 0);
            c.start();

            // Create Bricks on the environment.
            // Create some bricks around the environment.
            if (CREATEBRICKS) {
                Random rand = new Random();
                for (int x = 0; x < World.getInstance().getEnvironmentWidth(); x = x + 50) {
                    for (int y = 0; y < World.getInstance().getEnvironmentHeight(); y = y + 50) {

                        // Valid bricks are at least at a distance of 50 units from the creature and
                        // from the destination in both axis.
                        if (abs(crX - x) > 50 && abs(crY - y) > 50
                                && abs(destinationX - x) > 50 && abs(destinationY - y) > 50) {
                            // coordinates are ok. Discard randomly most of them so there is no overcrowding.
                            int r = rand.nextInt(10);
                            int xDim = rand.nextInt(35);
                            int yDim = rand.nextInt(35);
                            if (r == 0) {
                                World.createBrick(rand.nextInt(6), x, y, x + xDim, y + yDim);
                            }
                        }
                    }
                }
            }
        } catch (CommandExecException e) {

        }
        System.out.println("Robot " + c.getName() + " is ready to go.");

    }
}
