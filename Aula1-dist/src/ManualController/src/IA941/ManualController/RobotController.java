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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ia941
 */
public class RobotController {
    
        // Movement and Rotation constants
        private static final double MOVE_SPEED = 2.0f;
        private static final double ROTATION_SPEED = 4.0f;
        private static final long STOP_MOVEMENT_DELAY = 300;
        private static final long STOP_ROTATION_DELAY = 1000;
        
        private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
        
        // World handles
        private Creature mCreature;
        private WorldPoint mPosition;
    
        // Runnable to stop the creature movement, out of thread.
        final Runnable stopMovement = new Runnable() {
            public void run() {
                try{
                    mCreature.stop();
                } catch (CommandExecException e) {
                    System.out.println("Command error");
                }
            }
        };
                   
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
            } catch (CommandExecException e) {
                System.out.println("Erro capturado"); 
            }
        }
        
        public void moveForward() {
            try{
                mCreature.start();
                mCreature.move(MOVE_SPEED, MOVE_SPEED, 0);

                // Stop movement after a delay to avoid blocking the UI thread.
                mScheduler.schedule(stopMovement, STOP_MOVEMENT_DELAY, TimeUnit.MILLISECONDS);
            } catch (CommandExecException e) {
                System.out.println("Command error"); 
            } 
        }
        
        public void moveBackwards() {
            try{
                mCreature.start();
                mCreature.move(-MOVE_SPEED, -MOVE_SPEED, 0);

                // Stop movement after a delay to avoid blocking the UI thread.
                mScheduler.schedule(stopMovement, STOP_MOVEMENT_DELAY, TimeUnit.MILLISECONDS);
            } catch (CommandExecException e) {
                System.out.println("Command error"); 
            } 
        }
        
        public void turnRight() {
            try{
                mCreature.start();
                mCreature.rotate(ROTATION_SPEED);
                
                // Stop rotation after a delay to avoid blocking the UI thread.
                mScheduler.schedule(stopMovement, STOP_ROTATION_DELAY, TimeUnit.MILLISECONDS);
            } catch (CommandExecException e) {
                System.out.println("Command error"); 
            } 
        }
        
        public void turnLeft() {
            try{
                mCreature.start();
                mCreature.rotate(-ROTATION_SPEED);
                
                // Stop rotation after a delay to avoid blocking the UI thread.
                mScheduler.schedule(stopMovement, STOP_ROTATION_DELAY, TimeUnit.MILLISECONDS);
            } catch (CommandExecException e) {
                System.out.println("Command error"); 
            } 
            
        }

}
