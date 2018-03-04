/*
 * IA941 - Laboratório de Arquiteturas Cognitivas
 * 1S2018 - FEEC - Unicamp
 * Autor: Fabio Grassiotto - RA 890441
 */
package IA941.ManualController;

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
                //mCreature.start();
                mPosition = mCreature.getPosition();
                double pitch = mCreature.getPitch();
                double fuel = mCreature.getFuel();
                //c.moveto(1, 200, 200);            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void moveForward() {
            try{
                
            // Get current position and pitch (direction)
            double x, y, pitch, newX, newY;
            x = mPosition.getX();
            y = mPosition.getY();
            pitch = mCreature.getPitch();
            
            // New coordinates after moving forward
            newX = x + Math.cos(pitch);
            newY = y + Math.sin(pitch);
            
            // Move to the new position, with velocity = 1.
            mCreature.moveto(1, newX, newY);
            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void moveBackwards() {
            try{
                
            // Get current position and pitch (direction)
            double x, y, pitch, newX, newY;
            x = mPosition.getX();
            y = mPosition.getY();
            pitch = mCreature.getPitch();
            
            // New coordinates after moving forward
            newX = x - Math.cos(pitch);
            newY = y - Math.sin(pitch);
            
            // Move to the new position, with velocity = 1.
            mCreature.moveto(1, newX, newY);
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            }
        }
}
