/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SoarBridge;

import Simulation.Environment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jsoar.kernel.Agent;
import org.jsoar.kernel.Phase;
import org.jsoar.kernel.RunType;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.kernel.memory.Wmes;
import org.jsoar.kernel.symbols.DoubleSymbol;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.StringSymbol;
import org.jsoar.kernel.symbols.Symbol;
import org.jsoar.kernel.symbols.SymbolFactory;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.commands.SoarCommands;
import ws3dproxy.CommandExecException;
import ws3dproxy.CommandUtility;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/**
 *
 * @author Danilo Lucentini and Ricardo Gudwin
 */
public class SoarBridge
{
    // Log Variable
    Logger logger = Logger.getLogger(SoarBridge.class.getName());

    // SOAR Variables
    Agent agent = null;
    public Identifier inputLink = null;

    // Entity Variables
    Identifier creature;
    Identifier creatureSensor;
    Identifier creatureParameters;
    Identifier creaturePosition;
    Identifier creatureMemory;
    Identifier deliverySpot;
    
    Environment env;
    public Creature c;
    public String input_link_string = "";
    public String output_link_string = "";
    
    // Constants and variables for the leaflet Jewels.
    final private static String COLOR_RED = "Red";
    final private static String COLOR_GREEN = "Green";
    final private static String COLOR_BLUE = "Blue";
    final private static String COLOR_YELLOW = "Yellow";
    final private static String COLOR_MAGENTA = "Magenta";
    final private static String COLOR_WHITE = "White";
    
    // Variables for the collected Jewels.
    private int collectedRed = 0;
    private int collectedGreen = 0;
    private int collectedBlue = 0;
    private int collectedYellow = 0;
    private int collectedMagenta = 0;
    private int collectedWhite = 0;
    
    // Variables for MemoryEntity
    private boolean memoryEntityInit = false;
    private String memoryEntityName = null;
    private float memoryEntityX;
    private float memoryEntityY;
    
    /**
     * Constructor class
     * @param _e Environment
     * @param path Path for Rule Base
     * @param startSOARDebugger set true if you wish the SOAR Debugger to be started
     */
    public SoarBridge(Environment _e, String path, Boolean startSOARDebugger) 
    {
        env = _e;
        c = env.getCreature();
        try
        {
            ThreadedAgent tag = ThreadedAgent.create();
            agent = tag.getAgent();
            SoarCommands.source(agent.getInterpreter(), path);
            inputLink = agent.getInputOutput().getInputLink();

            // Initialize entities
            creature = null;

            // Debugger line
            if (startSOARDebugger)
            {
                agent.openDebugger();
            }
        }
        catch (Exception e)
        {
            logger.severe("Error while creating SOAR Kernel");
            e.printStackTrace();
        }
    }

    private Identifier CreateIdWME(Identifier id, String s) {
        SymbolFactory sf = agent.getSymbols();
        Identifier newID = sf.createIdentifier('I');
        agent.getInputOutput().addInputWme(id, sf.createString(s), newID);
        return(newID);
    }
    
    private void CreateFloatWME(Identifier id, String s, double value) {
        SymbolFactory sf = agent.getSymbols();
        DoubleSymbol newID = sf.createDouble(value);
        agent.getInputOutput().addInputWme(id, sf.createString(s), newID);
    }
    
    private void CreateStringWME(Identifier id, String s, String value) {
        SymbolFactory sf = agent.getSymbols();
        StringSymbol newID = sf.createString(value);
        agent.getInputOutput().addInputWme(id, sf.createString(s), newID);
    }
    
    private String getItemType(int categoryType)
    {
        String itemType = null;

        switch (categoryType)
        {
            case Constants.categoryBRICK:
                itemType = "BRICK";
                break;
            case Constants.categoryJEWEL:
                itemType = "JEWEL";
                break;
            case Constants.categoryFOOD:
            case Constants.categoryNPFOOD:
            case Constants.categoryPFOOD:
                itemType = "FOOD";
                break;
            case Constants.categoryCREATURE:
                itemType = "CREATURE";
                break;
        }
        return itemType;
    }
    
    
    /**
     * Create the WMEs at the InputLink of SOAR
     */
    private void prepareInputLink() 
    {
        //SymbolFactory sf = agent.getSymbols();
        Creature c = env.getCreature();
        inputLink = agent.getInputOutput().getInputLink();
        try
        {
            if (agent != null)
            {
              //SimulationCreature creatureParameter = (SimulationCreature)parameter;
              // Initialize Creature Entity
              creature = CreateIdWME(inputLink, "CREATURE");
              // Initialize Creature Memory
              creatureMemory = CreateIdWME(creature, "MEMORY");
              // Add entity in memory if necessary.
              if (memoryEntityInit) {
                Identifier entity = CreateIdWME(creatureMemory, "ENTITY");
                CreateStringWME(entity, "NAME", memoryEntityName);
                CreateFloatWME(entity, "X", memoryEntityX);
                CreateFloatWME(entity, "Y", memoryEntityY);
              }
              //Initialize Delivery Spot
              deliverySpot = CreateIdWME(creature, "DELIVERYSPOT");
              CreateFloatWME(deliverySpot, "X", 0.0);
              CreateFloatWME(deliverySpot, "Y", 0.0);
              CreateFloatWME(deliverySpot, "DISTANCE", 
                      GetGeometricDistanceToCreature(0.0,0.0,0.0,0.0,c.getPosition().getX(),c.getPosition().getY()));  
              
              // Set Creature Parameters
              Calendar lCDateTime = Calendar.getInstance();
              creatureParameters = CreateIdWME(creature, "PARAMETERS");
              CreateFloatWME(creatureParameters, "MINFUEL", 400);
              CreateFloatWME(creatureParameters, "TIMESTAMP", lCDateTime.getTimeInMillis());
              // Setting creature Position
              creaturePosition = CreateIdWME(creature, "POSITION");
              CreateFloatWME(creaturePosition, "X", c.getPosition().getX());
              CreateFloatWME(creaturePosition, "Y", c.getPosition().getY());
              // Set creature sensors
              creatureSensor = CreateIdWME(creature, "SENSOR");
              // Create Fuel Sensors
              Identifier fuel = CreateIdWME(creatureSensor, "FUEL");
              CreateFloatWME(fuel, "VALUE",c.getFuel());
              // Create Visual Sensors
              Identifier visual = CreateIdWME(creatureSensor, "VISUAL");
              List<Thing> thingsList = (List<Thing>) c.getThingsInVision();
              for (Thing t : thingsList) 
                {
                 Identifier entity = CreateIdWME(visual, "ENTITY");
                 CreateFloatWME(entity, "DISTANCE", GetGeometricDistanceToCreature(t.getX1(),t.getY1(),t.getX2(),t.getY2(),c.getPosition().getX(),c.getPosition().getY()));                                                    
                 CreateFloatWME(entity, "X", t.getX1());
                 CreateFloatWME(entity, "Y", t.getY1());
                 CreateFloatWME(entity, "X2", t.getX2());
                 CreateFloatWME(entity, "Y2", t.getY2());
                 CreateStringWME(entity, "TYPE", getItemType(t.getCategory()));
                 CreateStringWME(entity, "NAME", t.getName());
                 CreateStringWME(entity, "COLOR",Constants.getColorName(t.getMaterial().getColor()));                                                    
                }
              // Create the creature leaflets in the input link.
              List<Leaflet> leafletList = c.getLeaflets();
              Identifier leaflet = CreateIdWME(creature, "LEAFLET");
              
              int leafletRed = 0, leafletGreen = 0, leafletBlue = 0, leafletYellow = 0,
                  leafletMagenta = 0, leafletWhite = 0;
              
              
              for (Leaflet l: leafletList)
                {  
                 // Get what to collect from leaflet.
                 HashMap<String, Integer> h = l.getWhatToCollect();


                 for (String key: h.keySet())
                  {
                     // Count all jewel ocurrences in the leaflets. 
                     if (key.equals(COLOR_RED)) {
                         leafletRed++;
                     } else if (key.equals(COLOR_GREEN)) {
                         leafletGreen++;
                     } else if (key.equals(COLOR_BLUE)) {
                         leafletBlue++;
                     } else if (key.equals(COLOR_YELLOW)) {
                         leafletYellow++;
                     } else if (key.equals(COLOR_MAGENTA)) {
                         leafletMagenta++;
                     } else {
                         leafletWhite++;
                     }   
                  } 
                }
                // Create leaflet in the inputlink. All three leaflets are summed up 
                // as a single list.
                CreateFloatWME(leaflet, COLOR_RED, leafletRed);
                CreateFloatWME(leaflet, COLOR_GREEN, leafletGreen);
                CreateFloatWME(leaflet, COLOR_BLUE, leafletBlue);
                CreateFloatWME(leaflet, COLOR_YELLOW, leafletYellow);
                CreateFloatWME(leaflet, COLOR_MAGENTA, leafletMagenta);
                CreateFloatWME(leaflet, COLOR_WHITE, leafletWhite);
                
                // Initialize current target structure, leaflet - collected jewels.                
                int targetRed = leafletRed - collectedRed;
                int targetGreen = leafletGreen - collectedGreen;
                int targetBlue = leafletBlue - collectedBlue;
                int targetYellow = leafletYellow - collectedYellow;
                int targetMagenta = leafletMagenta - collectedMagenta;
                int targetWhite = leafletWhite - collectedWhite;
                
                Identifier target = CreateIdWME(creature, "TARGET");
                CreateFloatWME(target, COLOR_RED, targetRed);
                CreateFloatWME(target, COLOR_GREEN, targetGreen);
                CreateFloatWME(target, COLOR_BLUE, targetBlue);
                CreateFloatWME(target, COLOR_YELLOW, targetYellow);
                CreateFloatWME(target, COLOR_MAGENTA, targetMagenta);
                CreateFloatWME(target, COLOR_WHITE, targetWhite);
                
            }
        }
        catch (Exception e)
        {
            logger.severe("Error while Preparing Input Link");
            e.printStackTrace();
        }
    }

    private double GetGeometricDistanceToCreature(double x1, double y1, double x2, double y2, double xCreature, double yCreature)
    {
          float squared_dist = 0.0f;
          double maxX = Math.max(x1, x2);
          double minX = Math.min(x1, x2);
          double maxY = Math.max(y1, y2);
          double minY = Math.min(y1, y2);

          if(xCreature > maxX)
          {
            squared_dist += (xCreature - maxX)*(xCreature - maxX);
          }
          else if(xCreature < minX)
          {
            squared_dist += (minX - xCreature)*(minX - xCreature);
          }

          if(yCreature > maxY)
          {
            squared_dist += (yCreature - maxY)*(yCreature - maxY);
          }
          else if(yCreature < minY)
          {
            squared_dist += (minY - yCreature)*(minY - yCreature);
          }

          return Math.sqrt(squared_dist);
    }

    private void resetSimulation() {
        agent.initialize();
    }
    
    /**
     * Run SOAR until HALT
     */
    private void runSOAR() 
    {
        agent.runForever(); 
    }
    
    private int stepSOAR() {
        agent.runFor(1, RunType.PHASES);
        Phase ph = agent.getCurrentPhase();
        if (ph.equals(Phase.INPUT)) return(0);
        else if (ph.equals(Phase.PROPOSE)) return(1);
        else if (ph.equals(Phase.DECISION)) return(2);
        else if (ph.equals(Phase.APPLY)) return(3);
        else if (ph.equals(Phase.OUTPUT)) {
            if (agent.getReasonForStop() == null) return(4);
            else return(5);
        }
        else return(6);
    }

    private String GetParameterValue(String par, int idx) {
        List<Wme> Commands = Wmes.matcher(agent).filter(agent.getInputOutput().getOutputLink());
        List<Wme> Parameters = Wmes.matcher(agent).filter(Commands.get(idx));
        String parvalue = "";
        for (Wme w : Parameters) 
           if (w.getAttribute().toString().equals(par)) parvalue = w.getValue().toString();
        return(parvalue);
    }
    
    
    /**
     * Process the OutputLink given by SOAR and return a list of commands to WS3D
     * @return A List of SOAR Commands
     */
    private ArrayList<Command> processOutputLink() 
    {
        ArrayList<Command> commandList = new ArrayList<Command>();

        try
        {
            if (agent != null)
            {
                List<Wme> Commands = Wmes.matcher(agent).filter(agent.getInputOutput().getOutputLink());

                int idx = 0;
                for (Wme com : Commands)
                {
                    String name  = com.getAttribute().asString().getValue();
                    Command.CommandType commandType = Enum.valueOf(Command.CommandType.class, name);
                    Command command = null;

                    switch(commandType)
                    {
                        case MOVE:
                            Float rightVelocity = null;
                            Float leftVelocity = null;
                            Float linearVelocity = null;
                            Float xPosition = null;
                            Float yPosition = null;
                            rightVelocity = tryParseFloat(GetParameterValue("VelR", idx));
                            leftVelocity = tryParseFloat(GetParameterValue("VelL", idx));
                            linearVelocity = tryParseFloat(GetParameterValue("Vel", idx));
                            xPosition = tryParseFloat(GetParameterValue("X", idx));
                            yPosition = tryParseFloat(GetParameterValue("Y", idx));
                            command = new Command(Command.CommandType.MOVE);
                            CommandMove commandMove = (CommandMove)command.getCommandArgument();
                            if (commandMove != null)
                            {
                                if (rightVelocity != null) commandMove.setRightVelocity(rightVelocity);
                                if (leftVelocity != null)  commandMove.setLeftVelocity(leftVelocity);
                                if (linearVelocity != null) commandMove.setLinearVelocity(linearVelocity);
                                if (xPosition != null) commandMove.setX(xPosition);
                                if (yPosition != null) commandMove.setY(yPosition);
                                commandList.add(command);
                            }
                            else
                            {
                                logger.severe("Error processing MOVE command");
                            }
                            break;

                        case GET:
                            String thingNameToGet = null;
                            String colorToGet = null;
                            command = new Command(Command.CommandType.GET);
                            CommandGet commandGet = (CommandGet)command.getCommandArgument();
                            if (commandGet != null)
                            {
                                thingNameToGet = GetParameterValue("Name", idx);
                                if (thingNameToGet != null) commandGet.setThingName(thingNameToGet);
                                commandList.add(command);
                                colorToGet = GetParameterValue("Color", idx);
                                updateCollectedJewels(colorToGet);
                            }
                            break;

                        case EAT:
                            String thingNameToEat = null;
                            command = new Command(Command.CommandType.EAT);
                            CommandEat commandEat = (CommandEat)command.getCommandArgument();
                            if (commandEat != null)
                            {
                                thingNameToEat = GetParameterValue("Name", idx);
                                if (thingNameToEat != null) commandEat.setThingName(thingNameToEat);
                                commandList.add(command);
                            }
                            break;
                            
                        case DELIVER:
                            command = new Command(Command.CommandType.DELIVER);
                            CommandDeliver commandDeliver = (CommandDeliver) command.getCommandArgument();
                            if (commandDeliver != null) 
                            {
                                // Get leafletIds to deliver
                                Creature c = env.getCreature();
                                List<Leaflet> leafletList = c.getLeaflets();
                                for(Leaflet l: leafletList) 
                                {
                                    commandDeliver.setLeafletId(l.getID().toString());
                                    commandList.add(command);
                                }
                            }
                            break;
                        
                        case ADD_MEM:
                            memoryEntityInit = true;
                            memoryEntityName = GetParameterValue("Name", idx);
                            memoryEntityX = tryParseFloat(GetParameterValue("X", idx));
                            memoryEntityY = tryParseFloat(GetParameterValue("Y", idx));                            
                            break;
                            
                        case REMOVE_MEM:
                            memoryEntityInit = false;
                            memoryEntityName = null;
                            memoryEntityX = 0;
                            memoryEntityY = 0;
                            break;
                        default:
                            break;
                    }   
                idx++;
                }
            }
        }
        catch (Exception e)
        {
            logger.severe("Error while processing commands");
            e.printStackTrace();
        }

        return ((commandList.size() > 0) ? commandList : null);
    }
    
    void updateCollectedJewels(String color) {
        if (color.equals(COLOR_RED)) {
            collectedRed++;
        } else if (color.equals(COLOR_GREEN)) {
            collectedGreen++;
        } else if (color.equals(COLOR_BLUE)) {
            collectedBlue++;
        } else if (color.equals(COLOR_YELLOW)) {
            collectedYellow++;
        } else if (color.equals(COLOR_MAGENTA)) {
            collectedMagenta++;
        } else {
            collectedWhite++;
        }   
    }
    /**
     * Perform a complete SOAR step
     * @throws ws3dproxy.CommandExecException
     */
    public void step() throws CommandExecException
    {
        if (phase != -1) finish_msteps();
        resetSimulation();
        c.updateState();
        prepareInputLink();
        input_link_string = stringInputLink();
        //printInputWMEs();
        runSOAR();
        output_link_string = stringOutputLink();
        //printOutputWMEs();
        List<Command> commandList = processOutputLink();
        processCommands(commandList);
        //resetSimulation();
    }
    
    
    public void prepare_mstep() {
        resetSimulation();
        c.updateState();
        prepareInputLink();
        input_link_string = stringInputLink();
    }
    
    public int phase=-1;
    public void mstep() throws CommandExecException
    {
        if (phase == -1) prepare_mstep();
        phase = stepSOAR();
        if (phase == 5) {
            post_mstep();
            phase = -1;
        }
    }
    
    public void finish_msteps() throws CommandExecException {
        while (phase != -1) mstep();
    }
    
    public void post_mstep() throws CommandExecException {
        output_link_string = stringOutputLink();
        //printOutputWMEs();
        List<Command> commandList = processOutputLink();
        processCommands(commandList);
        //resetSimulation();
    }

    private void processCommands(List<Command> commandList) throws CommandExecException
    {

        if (commandList != null)
        {
            for (Command command:commandList)
            {
                switch (command.getCommandType())
                {
                    case MOVE:
                        processMoveCommand((CommandMove)command.getCommandArgument());
                    break;

                    case GET:
                        processGetCommand((CommandGet)command.getCommandArgument());
                    break;

                    case EAT:
                        processEatCommand((CommandEat)command.getCommandArgument());
                    break;

                    case DELIVER:
                        processDeliverCommand((CommandDeliver)command.getCommandArgument());
                    break;
                        
                    case STOP:
                        processStopCommand((CommandStop)command.getCommandArgument());
                    break;
                    
                    default:
                        System.out.println("Nenhum comando definido ...");
                        // Do nothing
                    break;
                }
            }
        }
        else System.out.println("comando nulo ...");
    }

    /**
     * Send Move Command to World Server
     * @param soarCommandMove Soar Move Command Structure
     */
    private void processMoveCommand(CommandMove soarCommandMove) throws CommandExecException
    {
        if (soarCommandMove != null)
        {
            if (soarCommandMove.getX() != null && soarCommandMove.getY() != null)
            {
                CommandUtility.sendGoTo("0", soarCommandMove.getRightVelocity(), soarCommandMove.getLeftVelocity(), soarCommandMove.getX(), soarCommandMove.getY());
            }
            else
            {
                CommandUtility.sendSetTurn("0",soarCommandMove.getLinearVelocity(),soarCommandMove.getRightVelocity(),soarCommandMove.getLeftVelocity());
            }
        }
        else
        {
            logger.severe("Error processing processMoveCommand");
        }
    }

    /**
     * Send Get Command to World Server
     * @param soarCommandGet Soar Get Command Structure
     */
    private void processGetCommand(CommandGet soarCommandGet) throws CommandExecException
    {
        if (soarCommandGet != null)
        {
            c.putInSack(soarCommandGet.getThingName());
        }
        else
        {
            logger.severe("Error processing processMoveCommand");
        }
    }

     /**
     * Send Eat Command to World Server
     * @param soarCommandEat Soar Eat Command Structure
     */
    private void processEatCommand(CommandEat soarCommandEat) throws CommandExecException
    {
        if (soarCommandEat != null)
        {
            c.eatIt(soarCommandEat.getThingName());
        }
        else
        {
            logger.severe("Error processing processMoveCommand");
        }
    }
    
    /**
     * Send Deliver Command to World Server
     * @param soarCommandDeliver Soar Move Command Structure
     */
    private void processDeliverCommand(CommandDeliver soarCommandDeliver) throws CommandExecException
    {
        if (soarCommandDeliver != null)
        {
            CommandUtility.sendDeliverLeaflet("0", soarCommandDeliver.getLeafletId());
        }
        else
        {
            logger.severe("Error processing processDeliverCommand");
        }
    }
    
    /**
     * Send Stop Command to World Server
     * @param soarCommandStop Soar Move Command Structure
     */
    private void processStopCommand(CommandStop soarCommandStop) throws CommandExecException
    {
        if (soarCommandStop != null)
        {
            CommandUtility.sendStopCreature("0");
        }
        else
        {
            logger.severe("Error processing processDeliverCommand");
        }
    }
    
    /**
     * Try Parse a Float Element
     * @param value Float Value
     * @return The Float Value or null otherwise
     */
    private Float tryParseFloat (String value)
    {
        Float returnValue = null;

        try
        {
            returnValue = Float.parseFloat(value);
        }
        catch (Exception ex)
        {
            returnValue = null;
        }

        return returnValue;
    }
    
    public void printWME(Identifier id) {
        printWME(id,0);
        
    }
    
    public void printWME(Identifier id, int level) {
        Iterator<Wme> It = id.getWmes();
        while (It.hasNext()) {
            Wme wme = It.next();
            Identifier idd = wme.getIdentifier();
            Symbol a = wme.getAttribute();
            Symbol v = wme.getValue();
            Identifier testv = v.asIdentifier();
            for (int i=0;i<level;i++) System.out.print("   ");
            if (testv != null) {
                System.out.print("("+idd.toString()+","+a.toString()+","+v.toString()+")\n");
                printWME(testv,level+1);
            }
            else System.out.print("("+idd.toString()+","+a.toString()+","+v.toString()+")\n");
        }   
    }
    
    public void printInputWMEs(){
        Identifier il = agent.getInputOutput().getInputLink();
        System.out.println("Input --->");
        printWME(il);
    }
    
    public void printOutputWMEs(){
        Identifier ol = agent.getInputOutput().getOutputLink();
        System.out.println("Output --->");
        printWME(ol);
    }
    
    public String stringWME(Identifier id) {
        String out = stringWME(id,0);
        return(out);
    }
    
    public String stringWME(Identifier id, int level) {
        String out="";
        Iterator<Wme> It = id.getWmes();
        while (It.hasNext()) {
            Wme wme = It.next();
            Identifier idd = wme.getIdentifier();
            Symbol a = wme.getAttribute();
            Symbol v = wme.getValue();
            Identifier testv = v.asIdentifier();
            for (int i=0;i<level;i++) out += "   ";
            if (testv != null) {
                out += "("+idd.toString()+","+a.toString()+","+v.toString()+")\n";
                out += stringWME(testv,level+1);
            }
            else out += "("+idd.toString()+","+a.toString()+","+v.toString()+")\n";
        }
       return(out); 
    }
    
    public String stringInputLink() {
        Identifier il = agent.getInputOutput().getInputLink();
        String out = stringWME(il);
        return(out);
    }
    
    public String stringOutputLink() {
        Identifier ol = agent.getInputOutput().getOutputLink();
        String out = stringWME(ol);
        return(out);
    }
    
    public Identifier getInitialState() {
        Set<Wme> allmem = agent.getAllWmesInRete();
        for (Wme w : allmem) {
            Identifier id = w.getIdentifier();
            if (id.toString().equalsIgnoreCase("S1"))
                return(id);
        }
        return(null);
    }
    
    public List<Identifier> getStates() {
        List<Identifier> li = new ArrayList<Identifier>();
        Set<Wme> allmem = agent.getAllWmesInRete();
        for (Wme w : allmem) {
            Identifier id = w.getIdentifier();
            if (id.isGoal()) {
                boolean alreadythere = false;
                for (Identifier icand : li)
                    if (icand == id) alreadythere = true;
                if (alreadythere == false) {
                    li.add(id);
                }
            }
        }
        return(li);
    }
    
    public Set<Wme> getWorkingMemory() {
        return(agent.getAllWmesInRete());
    }
}
