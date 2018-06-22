/** ***************************************************************************
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
 **************************************************************************** */
package memory;

import java.awt.Polygon;
import java.util.List;
import ws3dproxy.model.WorldPoint;
import ws3dproxy.model.Leaflet;

/**
 *
 * @author rgudwin
 */
public class CreatureInnerSense {

    public WorldPoint position;
    public double pitch;
    public double fuel;
    public Polygon FOV;
    public List<Leaflet> leaflets;

    @Override
    public String toString() {
        String retStr;
        if (position != null) {
            retStr = "Position: (" + (int) position.getX() + "," + (int) position.getY() + ") Pitch: " + (int) pitch + " Fuel: " + fuel;

        } else {
            retStr = "Position: null,null" + " Pitch: " + pitch + " Fuel: " + fuel;
        }
        // Add leaflets to Mindview display.
        if (leaflets != null) {
            for (Leaflet l : leaflets) {
                retStr += " Leaflet: " + l.toString() + " ";
            }
        }
        return retStr;
    }
}
