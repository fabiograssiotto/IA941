/*
 * IA941 - Laboratório de Arquiteturas Cognitivas
 * 1S2018 - FEEC - Unicamp
 * Autor: Fabio Grassiotto - RA 890441
 */
package IA941.ManualController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;
import ws3dproxy.model.WorldPoint;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

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
    private static double mFuel = 0;

    // UI handle
    final private ControllerUi mUi;

    // Runnable to stop the creature movement, out of thread.
    final Runnable stopMovement = new Runnable() {
        public void run() {
            try {
                mCreature.stop();
            } catch (CommandExecException e) {
                System.out.println("Command error");
            }
        }
    };

    // Runnable to update the fuel ammount.
    final Runnable fuelRunnable = new Runnable() {
        public void run() {
            mCreature.updateState();
            mFuel = mCreature.getFuel();
            mUi.setFuel(mFuel);
        }
    };

    RobotController(ControllerUi ui) {
        mUi = ui;
    }

    public void createWorld() {
        WS3DProxy proxy = new WS3DProxy();
        World w = null;

        System.out.println("Creating World...");
        try {
            w = proxy.getWorld();
            w.reset();
            mCreature = proxy.createCreature(100, 100, 0, 1);
            mCreature.start();
            w.grow(1);

            mFuel = mCreature.getFuel();
            mUi.setFuel(mFuel);
        } catch (CommandExecException e) {
            System.out.println("Erro capturado");
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(fuelRunnable, 1, 1, TimeUnit.SECONDS);
    }

    public void turnUpwards() {
        System.out.println("Turn Upwards");
        try {
            mCreature.updateState();
            mPosition = mCreature.getPosition();
            mCreature.moveto(3.0, mPosition.getX(), mPosition.getY() - 20);
            mCreature.stop();
        } catch (CommandExecException e) {
            System.out.println("Command error");
        }
    }

    public void turnDownwards() {
        System.out.println("Turn Downwards");
        try {
            mCreature.updateState();
            mPosition = mCreature.getPosition();
            mCreature.moveto(3.0, mPosition.getX(), mPosition.getY() + 20);
            mCreature.stop();
        } catch (CommandExecException e) {
            System.out.println("Command error");
        }
    }

    public void turnRight() {
        System.out.println("Turn Right");
        try {
            mCreature.updateState();
            mPosition = mCreature.getPosition();
            mCreature.moveto(3.0, mPosition.getX() + 20, mPosition.getY());
            mCreature.stop();
        } catch (CommandExecException e) {
            System.out.println("Command error");
        }
    }

    public void turnLeft() {
        System.out.println("Turn Left");
        try {
            mCreature.updateState();
            mPosition = mCreature.getPosition();
            mCreature.moveto(3.0, mPosition.getX() - 20, mPosition.getY());
            mCreature.stop();
        } catch (CommandExecException e) {
            System.out.println("Command error");
        }
    }

    public void walk() {
        System.out.println("Walk");
        try {
            mCreature.start();
            mCreature.move(MOVE_SPEED, MOVE_SPEED, 0);

            // Stop movement after a delay to avoid blocking the UI thread.
            mScheduler.schedule(stopMovement, STOP_MOVEMENT_DELAY, TimeUnit.MILLISECONDS);
        } catch (CommandExecException e) {
            System.out.println("Command error");
        }
    }

    public double getFuel() {
        return mFuel;
    }

    public void eatFoodOrPickJewel() {

        mCreature.updateState();
        // Create a copy of the list of things in vision to avoid concurrency.
        List<Thing> thingsInVision = new ArrayList<Thing>(mCreature.getThingsInVision());

        if (!thingsInVision.isEmpty()) {
            Collections.sort(thingsInVision, new Comparator<Thing>() {
                @Override
                public int compare(Thing o1, Thing o2) {
                    if (mCreature.calculateDistanceTo(o1) < mCreature.calculateDistanceTo(o2)) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            try {
                // Now it is sorted based on distance.
                Thing entity = thingsInVision.get(0);
                String entityName = entity.getName();
                // Only do the action it if we are close to it :)
                if (mCreature.calculateDistanceTo(entity) < 50) {
                    if (entity.getCategory() == Constants.categoryJEWEL) {
                        System.out.println("Pick Jewel: " + entityName);
                        mCreature.putInSack(entityName);
                        mUi.addPickedJewel();
                    } else {
                        System.out.println("Eat Food: " + entityName);
                        mCreature.eatIt(entityName);
                        mUi.addFoodEaten();
                    }
                }
            } catch (CommandExecException ex) {
                System.out.println("Error eating food");
            }
        }
    }
}
