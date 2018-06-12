package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import static java.lang.Math.abs;
import java.util.Map;
import java.util.Random;
import pathfinding.Example;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;
import view.CurrentActionFrame;

public class Environment extends EnvironmentImpl {

    private static final int DEFAULT_TICKS_PER_RUN = 100;
    private static final int DISTANCE_TO_BRICK = 80;
    private static final int BRICK_MANOUVER_DIST = 100;
    private int ticksPerRun;
    private WS3DProxy proxy;
    private Creature creature;
    private String currentAction;
    private Thing brick;
    private Object freespace;
    final private CurrentActionFrame actionPane;

    public Environment() {
        this.ticksPerRun = DEFAULT_TICKS_PER_RUN;
        this.proxy = new WS3DProxy();
        this.creature = null;
        //this.currentAction = "rotate";
        this.currentAction = "doNothing";
        this.brick = null;
        this.freespace = null;

        // Iniitalize Current Action Pane
        this.actionPane = new CurrentActionFrame();
        this.actionPane.setText(this.currentAction);
        this.actionPane.setVisible(true);
    }

    @Override
    public void init() {
        super.init();
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        taskSpawner.addTask(new BackgroundTask(ticksPerRun));

        try {
            System.out.println("Reseting the WS3D World ...");
            proxy.getWorld().reset();

            int crX = 100;
            int crY = 100;
            creature = proxy.createCreature(crX, crY, 0);

            // set initial destination as the same as the creature position.
            proxy.getWorld().setDestination(crX, crY);
            creature.start();
            System.out.println("Starting the WS3D Resource Generator ... ");
            //World.grow(1);

            if (true) {
                // Create some bricks around the environment.
                Random rand = new Random();
                for (int x = 0; x < World.getInstance().getEnvironmentWidth(); x = x + 100) {
                    for (int y = 0; y < World.getInstance().getEnvironmentHeight(); y = y + 100) {

                        // Valid bricks are at least at a distance of 50 units from the creature in
                        // the x and y axis. We do that so there are no problems for maneuvering around
                        // in the environment.
                        if (abs(crX - x) > 50 && abs(crY - y) > 50) {
                            // coordinates are ok. Discard randomly most of them so there is no overcrowding.
                            int r = rand.nextInt(4);
                            if (r == 0 || r == 1) {
                                World.createBrick(rand.nextInt(6), x, y, x + 10, y + 10);
                            }
                        }
                    }
                }
            }

            // Pathfinding
            Example ex = new Example();

            Thread.sleep(4000);
            creature.updateState();
            System.out.println("DemoLIDA has started...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BackgroundTask extends FrameworkTaskImpl {

        public BackgroundTask(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            updateEnvironment();
            performAction(currentAction);
        }
    }

    @Override
    public void resetState() {
        currentAction = "doNothing";
    }

    @Override
    public Object getState(Map<String, ?> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        switch (mode) {
            case "brick":
                requestedObject = brick;
                break;
            case "freespace":
                requestedObject = freespace;
                break;
            default:
                break;
        }
        return requestedObject;
    }

    public void updateEnvironment() {
        creature.updateState();
        brick = null;
        freespace = null;

        for (Thing thing : creature.getThingsInVision()) {
            if (thing.getCategory() == Constants.categoryBRICK
                    && creature.calculateDistanceTo(thing) <= DISTANCE_TO_BRICK) {
                // Identifies we are close to a brick.
                brick = thing;
                break;
            }
        }
        // In case that there are no bricks found in the visual memory, we have free space.
        if (brick == null) {
            freespace = new Object();
        }
    }

    @Override
    public void processAction(Object action) {
        String actionName = (String) action;
        currentAction = actionName.substring(actionName.indexOf(".") + 1);
        actionPane.setText(currentAction);
    }

    private void performAction(String currentAction) {
        try {

            System.out.println("Action: " + currentAction);
            switch (currentAction) {
                case "doNothing":
                    break;
                case "goToDestination": {
                    // Check if creature is already at the destination point.
                    int[] dest = proxy.getWorld().getDestination();
                    int crX = (int) creature.getPosition().getX();
                    int crY = (int) creature.getPosition().getY();

                    double dist = Math.sqrt((crX - dest[0]) * (crX - dest[0]) + (crY - dest[1]) * (crY - dest[1]));
                    System.out.println("Distance to destination: " + dist);
                    if (dist > 50) {
                        // only move if destination is far enough.
                        System.out.println("Creature destination X: " + dest[0] + " Y: " + dest[1]);
                        creature.start();
                        creature.moveto(3.0, dest[0], dest[1]);
                    } else {
                        // No need to move.
                        creature.stop();
                        resetState();
                        System.out.println("Creature at the destination");
                    }
                }
                break;
                case "avoidBrick":
                    //creature.move(0.0, 0.0, 0.0);
                    if (brick != null) {
                        // The action here should be to manouver the creature to avoid the wall.

                        double crX = creature.getPosition().getX();
                        double crY = creature.getPosition().getY();
                        double brX1 = brick.getX1();
                        double brX2 = brick.getX2();
                        double brY1 = brick.getY1();
                        double brY2 = brick.getY2();

                        double targetX, targetY;

                        // Check coordinates to drive the creature around the bricks in the environment.
                        if (crY <= brY1 && crX <= brX1) {
                            // 1st quadrant: creature is above and to the left of the brick.
                            targetX = (brX1 + brX2) / 2;
                            targetY = brY1 - BRICK_MANOUVER_DIST;
                        } else if (crY <= brY1 && crX >= brX2) {
                            // 2nd quadrant: above and to the right of the brick
                            targetX = (brX1 + brX2) / 2;
                            targetY = brY1 - BRICK_MANOUVER_DIST;
                        } else if (crY >= brY2 && crX <= brX1) {
                            // 3rd quadrant: below and to the left of the brick
                            targetX = (brX1 + brX2) / 2;
                            targetY = brY2 + BRICK_MANOUVER_DIST;
                        } else {
                            // 4th: below and to the right of the brick.
                            targetX = (brX1 + brX2) / 2;
                            targetY = brY2 + BRICK_MANOUVER_DIST;
                        }

                        creature.moveto(3.0, targetX, targetY);
                        System.out.println("Action: " + currentAction + " x:" + targetX + " y:" + targetY);
                    }
                    this.resetState();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
