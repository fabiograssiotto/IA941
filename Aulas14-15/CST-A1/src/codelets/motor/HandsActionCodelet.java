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
package codelets.motor;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import java.util.Random;
import java.util.logging.Logger;
import ws3dproxy.model.Creature;

/**
 * Hands Action Codelet monitors working storage for instructions and acts on the World accordingly.
 *
 * @author klaus
 *
 */
public class HandsActionCodelet extends Codelet {

    private MemoryObject handsMO;
    private String previousHandsAction = "";
    final private Creature c;
    final private Mind mind;
    final private Random r = new Random();
    static Logger log = Logger.getLogger(HandsActionCodelet.class.getCanonicalName());

    public HandsActionCodelet(Creature nc, Mind mind) {
        c = nc;
        this.mind = mind;
    }

    @Override
    public void accessMemoryObjects() {
        handsMO = (MemoryObject) this.getInput("HANDS");
    }

    public void proc() {

        String command = (String) handsMO.getI();

        if (!command.equals("") && (!command.equals(previousHandsAction))) {
            JSONObject jsonAction;
            try {
                jsonAction = new JSONObject(command);
                if (jsonAction.has("ACTION")) {
                    String action = jsonAction.getString("ACTION");
                    if (jsonAction.has("OBJECT")) {
                        String objectName = jsonAction.getString("OBJECT");
                        System.out.println("HANDS Action: " + action + " " + objectName);
                        if (action.equals("PICKUP")) {
                            try {
                                c.putInSack(objectName);
                            } catch (Exception e) {

                            }
                            log.info("Sending Put In Sack command to agent:****** " + objectName + "**********");
                        }
                        if (action.equals("EATIT")) {
                            try {
                                c.eatIt(objectName);
                            } catch (Exception e) {

                            }
                            log.info("Sending Eat command to agent:****** " + objectName + "**********");
                        }
                        if (action.equals("BURY")) {
                            try {
                                c.hideIt(objectName);
                            } catch (Exception e) {

                            }
                            log.info("Sending Bury command to agent:****** " + objectName + "**********");
                        }
                    } else {
                        System.out.println("HANDS Action: " + action);
                        if (action.equals("DELIVER")) {
                            try {
                                String leaf1 = jsonAction.getString("LEAFLET1");
                                String leaf2 = jsonAction.getString("LEAFLET2");
                                String leaf3 = jsonAction.getString("LEAFLET3");
                                c.deliverLeaflet(leaf1);
                                c.deliverLeaflet(leaf2);
                                c.deliverLeaflet(leaf3);

                                // Stop creature.
                                c.stop();

                                // Stop simulation
                                mind.shutDown();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        previousHandsAction = (String) handsMO.getI();
    }//end proc

    @Override
    public void calculateActivation() {

    }

}
