package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;

public class Environment extends EnvironmentImpl {

    private static final int DEFAULT_TICKS_PER_RUN = 100;
    private static final int DISTANCE_TO_BRICK = 80;
    private static final int BRICK_MANOUVER_DIST = 50;
    private int ticksPerRun;
    private WS3DProxy proxy;
    private Creature creature;
    private Thing food;
    private Thing jewel;
    private List<Thing> thingAhead;
    private Thing leafletJewel;
    private String currentAction;
    private Thing brick;
    private Object freespace;

    public Environment() {
        this.ticksPerRun = DEFAULT_TICKS_PER_RUN;
        this.proxy = new WS3DProxy();
        this.creature = null;
        this.food = null;
        this.jewel = null;
        this.thingAhead = new ArrayList<>();
        this.leafletJewel = null;
        //this.currentAction = "rotate";
        this.currentAction = "doNothing";
        this.brick = null;
        this.freespace = null;
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
                            int xDim = rand.nextInt(20);
                            int yDim = rand.nextInt(20);
                            World.createBrick(rand.nextInt(6), x, y, x + xDim, y + yDim);
                        }
                    }
                }
            }
            }

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
        //currentAction = "rotate";
        currentAction = "doNothing";
    }

    @Override
    public Object getState(Map<String, ?> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        switch (mode) {
            case "food":
                requestedObject = food;
                break;
            case "jewel":
                requestedObject = jewel;
                break;
            case "thingAhead":
                requestedObject = thingAhead;
                break;
            case "leafletJewel":
                requestedObject = leafletJewel;
                break;
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
        food = null;
        jewel = null;
        leafletJewel = null;
        thingAhead.clear();
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
        // In case that there are no bricks, we have free space.
        if (brick == null) {
            freespace  = new Object();
        }
    }

    @Override
    public void processAction(Object action) {
        String actionName = (String) action;
        currentAction = actionName.substring(actionName.indexOf(".") + 1);
    }

    private void performAction(String currentAction) {
        try {
            System.out.println("Action: " + currentAction);
            switch (currentAction) {
                case "rotate":
                    creature.rotate(1.0);
                    //CommandUtility.sendSetTurn(creature.getIndex(), -1.0, -1.0, 3.0);
                    break;
                case "gotoFood":
                    if (food != null) {
                        creature.moveto(3.0, food.getX1(), food.getY1());
                    }
                    //CommandUtility.sendGoTo(creature.getIndex(), 3.0, 3.0, food.getX1(), food.getY1());
                    break;
                case "gotoJewel":
                    if (leafletJewel != null) {
                        creature.moveto(3.0, leafletJewel.getX1(), leafletJewel.getY1());
                    }
                    //CommandUtility.sendGoTo(creature.getIndex(), 3.0, 3.0, leafletJewel.getX1(), leafletJewel.getY1());
                    break;
                case "get":
                    creature.move(0.0, 0.0, 0.0);
                    //CommandUtility.sendSetTurn(creature.getIndex(), 0.0, 0.0, 0.0);
                    if (thingAhead != null) {
                        for (Thing thing : thingAhead) {
                            if (thing.getCategory() == Constants.categoryJEWEL) {
                                creature.putInSack(thing.getName());
                            } else if (thing.getCategory() == Constants.categoryFOOD || thing.getCategory() == Constants.categoryNPFOOD || thing.getCategory() == Constants.categoryPFOOD) {
                                creature.eatIt(thing.getName());
                            }
                        }
                    }
                    this.resetState();
                    break;
                case "goToDestination":
                    double destX, destY;
                    destX = World.getInstance().getEnvironmentWidth();
                    destY = World.getInstance().getEnvironmentHeight();
                    creature.moveto(3.0, destX, destY);
                    System.out.println("Action: " + currentAction + " x:" + destX + " y:" + destY);
                    this.resetState();
                    break;
                case "doNothing":
                    break;
                case "avoidBrick":
                    //creature.move(0.0, 0.0, 0.0);
                    if (brick != null) {
                        // The action here should be to manouver the creature to avoid the wall.

                        double crX = creature.getPosition().getX();
                        double crY = creature.getPosition().getY();
                        double brY1 = brick.getY1();
                        double brY2 = brick.getY2();
                        

                        double targetX, targetY;

                        // Check coordinates to drive the creature around the bricks in the environment.
                        if (crY >= brick.getY2() || (crY >= brick.getY1() && crY <= brick.getY2())) {
                            // creature is below the brick.
                            // manouver from under it.
                            targetY = brick.getY2() + BRICK_MANOUVER_DIST;
                        } else {
                            // creature is above the brick.
                            targetY = brick.getY1() - BRICK_MANOUVER_DIST;
                        }

                        if (crX >= brick.getX2() || (crX >= brick.getX1() && crX <= brick.getX2())) {
                            targetX = brick.getX2() + BRICK_MANOUVER_DIST;
                        } else {
                            targetX = brick.getX1() - BRICK_MANOUVER_DIST;
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
