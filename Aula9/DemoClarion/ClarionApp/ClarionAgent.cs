﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Globalization;
using Clarion;
using Clarion.Framework;
using Clarion.Framework.Core;
using Clarion.Framework.Templates;
using ClarionApp.Model;
using ClarionApp;
using System.Threading;
using Gtk;

namespace ClarionApp
{
    /// <summary>
    /// Public enum that represents all possibilities of agent actions
    /// </summary>
    public enum CreatureActions
    {
        DO_NOTHING,
        ROTATE_CLOCKWISE,
        GO_AHEAD,
        GO_TO_JEWEL,
        GO_TO_FOOD,
		GET_JEWEL,
        GET_FOOD
    }

    public class ClarionAgent
    {
        #region Constants
        /// <summary>
        /// Constant that represents the Visual Sensor
        /// </summary>
        private String SENSOR_VISUAL_DIMENSION = "VisualSensor";
        /// <summary>
        /// Constant that represents that there is at least one wall ahead
        /// </summary>
        private String DIMENSION_WALL_AHEAD = "WallAhead";
		/// <summary>
		/// Constant that represents that there is at least one jewel ahead
		/// </summary>
		private String DIMENSION_JEWEL_AHEAD = "JewelAhead";
        /// <summary>
		/// Constant that represents that there is at least one jewel away from the creature
		/// </summary>
		private String DIMENSION_JEWEL_AWAY = "JewelAway";
        /// <summary>
        /// Constant that represents that there is at least one food item ahead
        /// </summary>
        private String DIMENSION_FOOD_AHEAD = "FoodAhead";
        /// <summary>
        /// Constant that represents that there is at least one food item away from the creature
        /// </summary>
        private String DIMENSION_FOOD_AWAY = "FoodAway";

        /// <summary>
        /// Activation Levels for the sensory information
        /// </summary>
        private double JEWEL_AHEAD_ACT_VAL = 1.0;
        private double FOOD_AHEAD_ACT_VAL = 0.8;
        private double JEWEL_AWAY_ACT_VAL = 0.7;
        private double FOOD_AWAY_ACT_VAL = 0.6;
        private double WALL_AHEAD_ACT_VAL = 0.3;
        private double MIN_ACT_VAL = 0.0;

        double prad = 0;
        #endregion

        #region Properties
		public MindViewer mind;
		String creatureId = String.Empty;
		String creatureName = String.Empty;
        #region Simulation
        /// <summary>
        /// If this value is greater than zero, the agent will have a finite number of cognitive cycle. Otherwise, it will have infinite cycles.
        /// </summary>
        public double MaxNumberOfCognitiveCycles = -1;
        /// <summary>
        /// Current cognitive cycle number
        /// </summary>
        private double CurrentCognitiveCycle = 0;
        /// <summary>
        /// Time between cognitive cycle in miliseconds
        /// </summary>
        public Int32 TimeBetweenCognitiveCycles = 0;
        /// <summary>
        /// A thread Class that will handle the simulation process
        /// </summary>
        private Thread runThread;
        #endregion

        #region Agent
		private WSProxy worldServer;
        /// <summary>
        /// The agent 
        /// </summary>
        private Clarion.Framework.Agent CurrentAgent;
        #endregion

        #region Perception Input
        /// <summary>
        /// Perception input to indicates a wall ahead
        /// </summary>
		private DimensionValuePair inputWallAhead;
		/// <summary>
		/// Perception input to indicates a jewel ahead
		/// </summary>
		private DimensionValuePair inputJewelAhead;
        /// <summary>
		/// Perception input to indicates a food item ahead
		/// </summary>
		private DimensionValuePair inputFoodAhead;
        /// <summary>
		/// Perception input to indicates a jewel away
		/// </summary>
		private DimensionValuePair inputJewelAway;
        /// <summary>
		/// Perception input to indicates a food item away
		/// </summary>
		private DimensionValuePair inputFoodAway;

        #endregion

        #region Action Output
        /// <summary>
        /// Output action that makes the agent to rotate clockwise
        /// </summary>
        private ExternalActionChunk outputRotateClockwise;
        /// <summary>
        /// Output action that makes the agent go ahead
        /// </summary>
		private ExternalActionChunk outputGoAhead;
		/// <summary>
		/// Output action that makes the agent get a jewel
		/// </summary>
		private ExternalActionChunk outputGetJewel;
		/// <summary>
		/// Output action that makes the agent eat some food
		/// </summary>
		private ExternalActionChunk outputGetFood;
        /// <summary>
        /// Output action that makes the agent go towards a jewel
        /// </summary>
        private ExternalActionChunk outputGoToJewel;
        /// <summary>
        /// Output action that makes the agent go towards a food item
        /// </summary>
        private ExternalActionChunk outputGoToFood;
        #endregion

        // Target jewels/food items
        Thing jewelToGet = null;
        Thing foodToGet = null;
        Thing jewelToGoTo = null;
        Thing foodToGoTo = null;

        #endregion

        #region Constructor
		public ClarionAgent(WSProxy nws, String creature_ID, String creature_Name)
        {
			worldServer = nws;
			// Initialize the agent
            CurrentAgent = World.NewAgent("Current Agent");
			mind = new MindViewer();
			mind.Show ();
			creatureId = creature_ID;
			creatureName = creature_Name;

            // Initialize Input Information
            inputWallAhead = World.NewDimensionValuePair(SENSOR_VISUAL_DIMENSION, DIMENSION_WALL_AHEAD);
			inputJewelAhead = World.NewDimensionValuePair(SENSOR_VISUAL_DIMENSION, DIMENSION_JEWEL_AHEAD);
            inputFoodAhead = World.NewDimensionValuePair(SENSOR_VISUAL_DIMENSION, DIMENSION_FOOD_AHEAD);
            inputJewelAway = World.NewDimensionValuePair(SENSOR_VISUAL_DIMENSION, DIMENSION_JEWEL_AWAY);
            inputFoodAway = World.NewDimensionValuePair(SENSOR_VISUAL_DIMENSION, DIMENSION_FOOD_AWAY);

            // Initialize Output actions
            outputRotateClockwise = World.NewExternalActionChunk(CreatureActions.ROTATE_CLOCKWISE.ToString());
            outputGoAhead = World.NewExternalActionChunk(CreatureActions.GO_AHEAD.ToString());
			outputGetJewel = World.NewExternalActionChunk(CreatureActions.GET_JEWEL.ToString());
            outputGetFood = World.NewExternalActionChunk(CreatureActions.GET_FOOD.ToString());
            outputGoToJewel = World.NewExternalActionChunk(CreatureActions.GO_TO_JEWEL.ToString());
            outputGoToFood = World.NewExternalActionChunk(CreatureActions.GO_TO_FOOD.ToString());

            //Create thread to simulation
            runThread = new Thread(CognitiveCycle);
			Console.WriteLine("Agent started");
        }
        #endregion

        #region Public Methods
        /// <summary>
        /// Run the Simulation in World Server 3d Environment
        /// </summary>
        public void Run()
        {                
			Console.WriteLine ("Running ...");
            // Setup Agent to run
            if (runThread != null && !runThread.IsAlive)
            {
                SetupAgentInfraStructure();
				// Start Simulation Thread                
                runThread.Start(null);
            }
        }

        /// <summary>
        /// Abort the current Simulation
        /// </summary>
        /// <param name="deleteAgent">If true beyond abort the current simulation it will die the agent.</param>
        public void Abort(Boolean deleteAgent)
        {   Console.WriteLine ("Aborting ...");
            if (runThread != null && runThread.IsAlive)
            {
                runThread.Abort();
            }

            if (CurrentAgent != null && deleteAgent)
            {
                CurrentAgent.Die();
            }
        }

		IList<Thing> processSensoryInformation()
		{
			IList<Thing> response = null;

			if (worldServer != null && worldServer.IsConnected)
			{
				response = worldServer.SendGetCreatureState(creatureName);
				prad = (Math.PI / 180) * response.First().Pitch;
				while (prad > Math.PI) prad -= 2 * Math.PI;
				while (prad < - Math.PI) prad += 2 * Math.PI;
				Sack s = worldServer.SendGetSack("0");
				mind.setBag(s);
			}

			return response;
		}

		void processSelectedAction(CreatureActions externalAction)
		{   Thread.CurrentThread.CurrentCulture = new CultureInfo("en-US");
			if (worldServer != null && worldServer.IsConnected)
			{
				switch (externalAction)
				{
				case CreatureActions.DO_NOTHING:
					// Do nothing as the own value says
					break;
				case CreatureActions.ROTATE_CLOCKWISE:
					worldServer.SendSetAngle(creatureId, 2, -2, 2);
					break;
				case CreatureActions.GO_AHEAD:
					worldServer.SendSetAngle(creatureId, 1, 1, prad);
					break;
				case CreatureActions.GET_JEWEL:
                    worldServer.SendSackIt(creatureId, jewelToGet.Name);
					break;
                case CreatureActions.GET_FOOD:
                    worldServer.SendSackIt(creatureId, foodToGet.Name);
                    break;
                case CreatureActions.GO_TO_JEWEL:
                    worldServer.SendSetAngle(creatureId, 0, 0, prad);
                    worldServer.SendSetGoTo(creatureId, 1, 1, jewelToGoTo.X1, jewelToGoTo.Y1);
                    break;
                case CreatureActions.GO_TO_FOOD:
                    worldServer.SendSetAngle(creatureId, 0, 0, prad);
                    worldServer.SendSetGoTo(creatureId, 1, 1, foodToGoTo.X1, foodToGoTo.Y1);
                    break;
                default:
					break;
				}
			}
		}

        #endregion

        #region Setup Agent Methods
        /// <summary>
        /// Setup agent infra structure (ACS, NACS, MS and MCS)
        /// </summary>
        private void SetupAgentInfraStructure()
        {
            // Setup the MS Subsystem
            SetupMS();
            // Setup the ACS Subsystem
            SetupACS();
        }

        private void SetupMS()
        {            
            //RichDrive
        }

        /// <summary>
        /// Setup the ACS subsystem
        /// </summary>
        private void SetupACS()
        {
            // Create Rule to avoid collision with wall
            SupportCalculator avoidCollisionWallSupportCalculator = FixedRuleToAvoidCollisionWall;
            FixedRule ruleAvoidCollisionWall = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputRotateClockwise, avoidCollisionWallSupportCalculator);

            // Commit this rule to Agent (in the ACS)
            CurrentAgent.Commit(ruleAvoidCollisionWall);

            // Create Colision To Go Ahead
            SupportCalculator goAheadSupportCalculator = FixedRuleToGoAhead;
            FixedRule ruleGoAhead = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGoAhead, goAheadSupportCalculator);
            
            // Commit this rule to Agent (in the ACS)
            CurrentAgent.Commit(ruleGoAhead);

			// Create Rule To Get Jewel(s)
			SupportCalculator getJewelSupportCalculator = FixedRuleToGetJewel;
			FixedRule ruleGetJewel = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGetJewel, getJewelSupportCalculator);

			// Commit this rule to Agent (in the ACS)
			CurrentAgent.Commit(ruleGetJewel);

            // Create Rule To Get Food(s)
            SupportCalculator getFoodSupportCalculator = FixedRuleToGetFood;
            FixedRule ruleGetFood = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGetFood, getFoodSupportCalculator);

            // Commit this rule to Agent (in the ACS)
            CurrentAgent.Commit(ruleGetFood);

            // Create Rule To Go To Jewel(s)
            SupportCalculator goToJewelSupportCalculator = FixedRuleToGoToJewel;
            FixedRule ruleGoToJewel = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGoToJewel, goToJewelSupportCalculator);

            // Commit this rule to Agent (in the ACS)
            CurrentAgent.Commit(ruleGoToJewel);

            // Create Rule To Get Food(s)
            SupportCalculator goToFoodSupportCalculator = FixedRuleToGoToFood;
            FixedRule ruleGoToFood = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGoToFood, goToFoodSupportCalculator);

            // Commit this rule to Agent (in the ACS)
            CurrentAgent.Commit(ruleGoToFood);

            // Disable Rule Refinement
            CurrentAgent.ACS.Parameters.PERFORM_RER_REFINEMENT = false;

            // The selection type will be probabilistic
            CurrentAgent.ACS.Parameters.LEVEL_SELECTION_METHOD = ActionCenteredSubsystem.LevelSelectionMethods.STOCHASTIC;

            // The action selection will be fixed (not variable) i.e. only the statement defined above.
            CurrentAgent.ACS.Parameters.LEVEL_SELECTION_OPTION = ActionCenteredSubsystem.LevelSelectionOptions.FIXED;

            // Define Probabilistic values
            CurrentAgent.ACS.Parameters.FIXED_FR_LEVEL_SELECTION_MEASURE = 1;
            CurrentAgent.ACS.Parameters.FIXED_IRL_LEVEL_SELECTION_MEASURE = 0;
            CurrentAgent.ACS.Parameters.FIXED_BL_LEVEL_SELECTION_MEASURE = 0;
            CurrentAgent.ACS.Parameters.FIXED_RER_LEVEL_SELECTION_MEASURE = 0;
        }

		/// <summary>
		/// Setup the NACS subsystem
		/// </summary>
		private void SetupNACS() {
			
		}

        /// <summary>
        /// Make the agent perception. In other words, translate the information that came from sensors to a new type that the agent can understand
        /// </summary>
        /// <param name="sensorialInformation">The information that came from server</param>
        /// <returns>The perceived information</returns>
		private SensoryInformation prepareSensoryInformation(IList<Thing> listOfThings)
        {
            // New sensory information
            SensoryInformation si = World.NewSensoryInformation(CurrentAgent);
            Sack sack;

            // Get information from sack and leaflets.
            int targetRed = 0;
            int targetGreen = 0;
            int targetBlue = 0;
            int targetYellow = 0;
            int targetMagenta = 0;
            int targetWhite = 0;

            Creature c = (Creature)listOfThings.Where(item => (item.CategoryId == Thing.CATEGORY_CREATURE)).First();
            // Update leaflets
            int n = 0;
            foreach (Leaflet l in c.getLeaflets())
            {
                targetRed += l.getRequired("Red");
                targetGreen += l.getRequired("Green");
                targetBlue += l.getRequired("Blue");
                targetYellow += l.getRequired("Yellow");
                targetMagenta += l.getRequired("Magenta");
                targetWhite += l.getRequired("White");
                mind.updateLeaflet(n, l);
                n++;
            }

            if (worldServer != null && worldServer.IsConnected)
            {
                sack = worldServer.SendGetSack("0");
                targetRed -= sack.red_crystal;
                targetGreen -= sack.green_crystal;
                targetBlue -= sack.blue_crystal;
                targetYellow -= sack.yellow_crystal;
                targetMagenta -= sack.magenta_crystal;
                targetWhite -= sack.white_crystal;
            }

            double wallAheadActivationValue = MIN_ACT_VAL;
            double jewelAheadActivationValue = MIN_ACT_VAL;
            double foodAheadActivationValue = MIN_ACT_VAL;
            double jewelAwayActivationValue = MIN_ACT_VAL;
            double foodAwayActivationValue = MIN_ACT_VAL;

            // Loop through the list of things in the environment.
            // First, handle close objects.
            foreach (Thing thing in listOfThings)
            {
                // Thing is close to the creature, so change activation values accordingly.
                int categoryId = thing.CategoryId;
                if (thing.DistanceToCreature <= 50 && categoryId != Thing.CATEGORY_CREATURE)
                {
                    switch (categoryId)
                    {
                        case Thing.CATEGORY_BRICK:
                            wallAheadActivationValue = WALL_AHEAD_ACT_VAL;
                            break;
                        case Thing.CATEGORY_JEWEL:
                            jewelAheadActivationValue = JEWEL_AHEAD_ACT_VAL;
                            jewelToGet = thing;
                            break;
                        case Thing.categoryPFOOD:
                            foodAheadActivationValue = FOOD_AHEAD_ACT_VAL;
                            foodToGet = thing;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Look now for the closest jewel to go to.
            IEnumerable<Thing> jewels = listOfThings.Where(item => (item.CategoryId == Thing.CATEGORY_JEWEL && item.DistanceToCreature > 50));
            jewels.OrderBy(item => item.DistanceToCreature);
            foreach(Thing jewel in jewels)
            {
                // Check if the jewel is required, otherwise skip.
                if ((jewel.Material.Color.Equals("Red") && targetRed > 0) ||
                     (jewel.Material.Color.Equals("Green") && targetGreen > 0) ||
                     (jewel.Material.Color.Equals("Blue") && targetBlue > 0) ||
                     (jewel.Material.Color.Equals("Yellow") && targetYellow > 0) ||
                     (jewel.Material.Color.Equals("Magenta") && targetMagenta > 0) ||
                     (jewel.Material.Color.Equals("White") && targetWhite > 0)) {
                    jewelAwayActivationValue = JEWEL_AWAY_ACT_VAL;
                    jewelToGoTo = jewel;
                }
            }

            // And the closest food to go to, if required.
            IEnumerable<Thing> foods = listOfThings.Where(item => (item.CategoryId == Thing.categoryPFOOD && item.DistanceToCreature > 50));
            if (c.Fuel < 400)
            {
                foodAwayActivationValue = FOOD_AWAY_ACT_VAL;
                foodToGoTo = foods.OrderBy(item => item.DistanceToCreature).First();
            }
            
            si.Add(inputWallAhead, wallAheadActivationValue);
            si.Add(inputJewelAhead, jewelAheadActivationValue);
            si.Add(inputFoodAhead, foodAheadActivationValue);
            si.Add(inputJewelAway, jewelAwayActivationValue);
            si.Add(inputFoodAway, foodAwayActivationValue);

            return si;
        }
        #endregion

        #region Fixed Rules
        private double FixedRuleToAvoidCollisionWall(ActivationCollection currentInput, Rule target)
        {
            // See partial match threshold to verify what are the rules available for action selection
            return ((currentInput.Contains(inputWallAhead, WALL_AHEAD_ACT_VAL))) ? 1.0 : 0.0;
        }

        private double FixedRuleToGoAhead(ActivationCollection currentInput, Rule target)
        {
            // Here we will make the logic to go ahead
            return ((currentInput.Contains(inputWallAhead, MIN_ACT_VAL))) ? 1.0 : 0.0;
        }

		private double FixedRuleToGetJewel(ActivationCollection currentInput, Rule target)
		{
			// Here we will make the logic to collect a jewel
			return ((currentInput.Contains(inputJewelAhead, JEWEL_AHEAD_ACT_VAL))) ? 1.0 : 0.0;
		}

        private double FixedRuleToGetFood(ActivationCollection currentInput, Rule target)
        {
            // Here we will make the logic to eat food
            return ((currentInput.Contains(inputFoodAhead, FOOD_AHEAD_ACT_VAL))) ? 1.0 : 0.0;
        }

        private double FixedRuleToGoToJewel(ActivationCollection currentInput, Rule target)
        {
            // Here we will make the logic to collect a jewel
            return ((currentInput.Contains(inputJewelAhead, JEWEL_AWAY_ACT_VAL))) ? 1.0 : 0.0;
        }

        private double FixedRuleToGoToFood(ActivationCollection currentInput, Rule target)
        {
            // Here we will make the logic to eat food
            return ((currentInput.Contains(inputFoodAhead, FOOD_AWAY_ACT_VAL))) ? 1.0 : 0.0;
        }
        #endregion

        #region Run Thread Method
        private void CognitiveCycle(object obj)
        {

			Console.WriteLine("Starting Cognitive Cycle ... press CTRL-C to finish !");
            // Cognitive Cycle starts here getting sensorial information
            while (CurrentCognitiveCycle != MaxNumberOfCognitiveCycles)
            {   
				// Get current sensory information                    
				IList<Thing> currentSceneInWS3D = processSensoryInformation();

                // Make the perception
                SensoryInformation si = prepareSensoryInformation(currentSceneInWS3D);

                //Perceive the sensory information
                CurrentAgent.Perceive(si);

                //Choose an action
                ExternalActionChunk chosen = CurrentAgent.GetChosenExternalAction(si);

                // Get the selected action
                String actionLabel = chosen.LabelAsIComparable.ToString();
                CreatureActions actionType = (CreatureActions)Enum.Parse(typeof(CreatureActions), actionLabel, true);

                // Call the output event handler
				processSelectedAction(actionType);

                // Increment the number of cognitive cycles
                CurrentCognitiveCycle++;

                //Wait to the agent accomplish his job
                if (TimeBetweenCognitiveCycles > 0)
                {
                    Thread.Sleep(TimeBetweenCognitiveCycles);
                }
			}
        }
        #endregion

    }
}
