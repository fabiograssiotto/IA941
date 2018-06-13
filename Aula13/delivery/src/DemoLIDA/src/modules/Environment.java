package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import pathfinding.Pathfinder;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;
import view.CurrentActionFrame;
import view.NoPlanFrame;

public class Environment extends EnvironmentImpl {

    private static final int DEFAULT_TICKS_PER_RUN = 100;
    private static final int DISTANCE_TO_BRICK = 80;
    private int ticksPerRun;
    private WS3DProxy proxy;
    private Creature creature;
    private String currentAction;
    private Thing brick;
    private Object freespace;
    final private CurrentActionFrame actionPane;
    final private NoPlanFrame noPlanPopup;
    private Pathfinder pathfinder;
    private int[] planDestination;

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

        this.noPlanPopup = new NoPlanFrame();
        this.noPlanPopup.setVisible(false);
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
                            int xDim = rand.nextInt(20);
                            int yDim = rand.nextInt(20);
                            if (r == 0 || r == 1) {
                                World.createBrick(rand.nextInt(6), x, y, x + xDim, y + yDim);
                            }
                        }
                    }
                }
            }

            // Pathfinding
            pathfinder = new Pathfinder(World.getInstance().getEnvironmentWidth(),
                    World.getInstance().getEnvironmentHeight());

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

                    // Check if creature is already at the final destination point.
                    if (creatureArrived()) {
                        resetState();
                        creature.stop();
                        pathfinder.reset();
                        System.out.println("Creature at the final destination");
                    } else {
                        // We need to get to the destination.
                        // Are we following a plan already?
                        if (pathfinder.hasPlan()) {
                            // Check if the creature has finished the current step.
                            if (creatureAt(planDestination[0], planDestination[1])) {
                                // current step is done, go to next one
                                planDestination = pathfinder.getNextDestination();
                                if (planDestination[0] != -1) {
                                    creature.moveto(3.0, planDestination[0], planDestination[1]);
                                    System.out.println("Creature moving to: " + planDestination[0] + " " + planDestination[1]);
                                } else {
                                    pathfinder.reset();
                                    System.out.println("Plan done");
                                }
                            } else {
                                // Keep moving on the current step.
                                creature.moveto(3.0, planDestination[0], planDestination[1]);
                                System.out.println("Creature moving to: " + planDestination[0] + " " + planDestination[1]);
                            }

                        } else {
                            // Create a plan to get to the final destination.
                            int crX = (int) creature.getPosition().getX();
                            int crY = (int) creature.getPosition().getY();
                            int[] finalDest = proxy.getWorld().getDestination();
                            List<Thing> bricks = getBricks();
                            pathfinder.replan(crX, crY, finalDest[0], finalDest[1], bricks);
                            // Now go to the first step in the plan.
                            planDestination = pathfinder.getNextDestination();
                            if (planDestination[0] == -1) {
                                // A plan to get to that destination could not be created.
                                System.out.println("Plan is not possible");
                                noPlanPopup.setVisible(true);
                            } else {
                                creature.start();
                                creature.moveto(3.0, planDestination[0], planDestination[1]);
                                System.out.println("Start new plan to get to: " + planDestination[0] + " " + planDestination[1]);
                            }
                        }
                    }

                }

                break;
                case "avoidBrick":
                    // Found a brick along the creature path, so we need to replan.
                    System.out.println("Replanning, found a brick");
                    if (!creatureArrived()) {
                        int crX = (int) creature.getPosition().getX();
                        int crY = (int) creature.getPosition().getY();
                        int[] finalDest = proxy.getWorld().getDestination();
                        List<Thing> bricks = getBricks();
                        pathfinder.reset();
                        pathfinder.replan(crX, crY, finalDest[0], finalDest[1], bricks);
                        // Now go to the first step in the plan.
                        planDestination = pathfinder.getNextDestination();
                        creature.moveto(3.0, planDestination[0], planDestination[1]);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean creatureArrived() {
        // Check if creature is already at the final destination point.
        try {
            int[] finalDest = proxy.getWorld().getDestination();
            return creatureAt(finalDest[0], finalDest[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean creatureAt(int x, int y) {
        // Check if creature is close to the point passed in.
        int crX = (int) creature.getPosition().getX();
        int crY = (int) creature.getPosition().getY();

        double dist = Math.sqrt((crX - x) * (crX - x) + (crY - y) * (crY - y));
        return dist < 50;
    }

    private List<Thing> getBricks() {
        List<Thing> brickList = new ArrayList<Thing>();
        for (Thing thing : creature.getThingsInVision()) {
            if (thing.getCategory() == Constants.categoryBRICK) {
                brickList.add(thing);
            }
        }
        return brickList;
    }
}
