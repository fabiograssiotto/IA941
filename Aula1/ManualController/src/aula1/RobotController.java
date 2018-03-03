/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aula1;

import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;
import ws3dproxy.model.WorldPoint;

/**
 *
 * @author ia941
 */
public class RobotController {
    
        private Creature mCreature;
        private WorldPoint mPosition;
    
        RobotController() {
        }
        
        public void createWorld() {
            WS3DProxy proxy = new WS3DProxy();
        
            try {   
                World w = World.getInstance();
                w.reset();
                World.createFood(0, 350, 75);
                World.createFood(0, 100, 220);
                World.createFood(0, 250, 210);
                mCreature = proxy.createCreature(100,450,0);
                mCreature.start();
                WorldPoint mPosition = mCreature.getPosition();
                double pitch = mCreature.getPitch();
                double fuel = mCreature.getFuel();
                //c.moveto(1, 200, 200);            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void moveForward() {
            try{
                
            mCreature.moveto(mPosition.getX()+1, mPosition.getY(), 0);
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            }
        }
        
}
