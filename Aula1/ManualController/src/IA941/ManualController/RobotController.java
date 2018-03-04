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
    
        private static final double MOVE_SPEED = 2.0f;
        private static final double TURN_SPEED = 90.0f;
        private static final int THREAD_SLEEP = 300;
        
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
                
            mCreature.start();
            mCreature.move(MOVE_SPEED, MOVE_SPEED, 0);
            Thread.sleep(THREAD_SLEEP);
            mCreature.stop();
            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            } catch (InterruptedException ex) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void moveBackwards() {
            try{
                
            mCreature.start();
            mCreature.move(-MOVE_SPEED, -MOVE_SPEED, 0);
            Thread.sleep(THREAD_SLEEP);
            mCreature.stop();
            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            } catch (InterruptedException ex) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void turnRight() {
            try{
                
            mCreature.start();
            mCreature.move(0, 0, Math.toRadians(TURN_SPEED));
            Thread.sleep(THREAD_SLEEP);
            mCreature.stop();
            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            } catch (InterruptedException ex) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void turnLeft() {
            try{
                
            mCreature.start();
            mCreature.move(0, 0, Math.toRadians(TURN_SPEED));
            Thread.sleep(THREAD_SLEEP);
            mCreature.stop();
            
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            } catch (InterruptedException ex) {
                System.out.println("Erro capturado"); 
            }
        }
}
