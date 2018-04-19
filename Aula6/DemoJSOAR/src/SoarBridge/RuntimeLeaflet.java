/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SoarBridge;

import java.util.HashMap;
import ws3dproxy.model.Leaflet;

/**
 *
 * @author ra890441
 */
public class RuntimeLeaflet extends Leaflet{
    
    public RuntimeLeaflet(Long ID, HashMap items, int payment, int situation) {
        super(ID, items, payment, situation);
    }
}
