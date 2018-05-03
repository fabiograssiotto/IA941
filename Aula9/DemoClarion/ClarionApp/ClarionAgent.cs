using System;
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
		GET_JEWEL,
		STOP
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
		/// Perception input to indicate all leaflets have been collected.
		/// </summary>
		private DimensionValuePair inputAllLeafletsDone;
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
		/// Output action that makes the agent stop
		/// </summary>
		private ExternalActionChunk outputStop;
        #endregion

		// List to specify jewel names to be collected.
		private List<String> jewelNames;

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

            // Initialize Output actions
            outputRotateClockwise = World.NewExternalActionChunk(CreatureActions.ROTATE_CLOCKWISE.ToString());
            outputGoAhead = World.NewExternalActionChunk(CreatureActions.GO_AHEAD.ToString());
			outputGetJewel = World.NewExternalActionChunk(CreatureActions.GET_JEWEL.ToString());
			outputStop = World.NewExternalActionChunk(CreatureActions.STOP.ToString());

			// Initialize list of jewel names to collect
			jewelNames = new List<string>();

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
					foreach (string jewelToGet in jewelNames) {
						worldServer.SendSackIt(creatureId, jewelToGet);
					}
					// reset list of jewels
					jewelNames.Clear();
					break;
				case CreatureActions.STOP:
					worldServer.SendStopCreature(creatureId);
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

            // Create Colission To Go Ahead
            SupportCalculator goAheadSupportCalculator = FixedRuleToGoAhead;
            FixedRule ruleGoAhead = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGoAhead, goAheadSupportCalculator);
            
            // Commit this rule to Agent (in the ACS)
            CurrentAgent.Commit(ruleGoAhead);

			// Create Rule To Get Jewel
			SupportCalculator getJewelSupportCalculator = FixedRuleToGetJewel;
			FixedRule ruleGetJewel = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputGetJewel, getJewelSupportCalculator);

			// Commit this rule to Agent (in the ACS)
			CurrentAgent.Commit(ruleGetJewel);

			// Create Rule To Stop after collecting all jewels.
			SupportCalculator stopSupportCalculator = FixedRuleToStop;
			FixedRule ruleStop = AgentInitializer.InitializeActionRule(CurrentAgent, FixedRule.Factory, outputStop, stopSupportCalculator);

			// Commit this rule to Agent (in the ACS)
			CurrentAgent.Commit(ruleStop);

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
        /// Make the agent perception. In other words, translate the information that came from sensors to a new type that the agent can understand
        /// </summary>
        /// <param name="sensorialInformation">The information that came from server</param>
        /// <returns>The perceived information</returns>
		private SensoryInformation prepareSensoryInformation(IList<Thing> listOfThings)
        {
            // New sensory information
            SensoryInformation si = World.NewSensoryInformation(CurrentAgent);

            // Detect if we have a wall ahead
            Boolean wallAhead = listOfThings.Where(item => (item.CategoryId == Thing.CATEGORY_BRICK && item.DistanceToCreature <= 61)).Any();
            double wallAheadActivationValue = wallAhead ? CurrentAgent.Parameters.MAX_ACTIVATION : CurrentAgent.Parameters.MIN_ACTIVATION;
            si.Add(inputWallAhead, wallAheadActivationValue);

			// Detect if we have a jewel to be collected
			Boolean jewelAhead = listOfThings.Where(item => (item.CategoryId == Thing.CATEGORY_JEWEL && item.DistanceToCreature <= 60)).Any();
			if (jewelAhead) {
				IEnumerable<Thing> jewels = listOfThings.Where (item => (item.CategoryId == Thing.CATEGORY_JEWEL && item.DistanceToCreature <= 60));
				foreach (Thing jewel in jewels) {
					jewelNames.Add(jewel.Name);
				}
			}
			double jewelAheadActivationValue = jewelAhead ? CurrentAgent.Parameters.MAX_ACTIVATION : CurrentAgent.Parameters.MIN_ACTIVATION;
			si.Add(inputJewelAhead, jewelAheadActivationValue);

			Creature c = (Creature) listOfThings.Where(item => (item.CategoryId == Thing.CATEGORY_CREATURE)).First();

			// Detect empty leaflets and set rule.
			int missingJewels = 0;
			foreach(Leaflet l in c.getLeaflets()) {
				missingJewels += l.getRequired("Red");
				missingJewels += l.getRequired("Green");
				missingJewels += l.getRequired("Blue");
				missingJewels += l.getRequired("Yellow");
				missingJewels += l.getRequired("Magenta");
				missingJewels += l.getRequired("White");
			}
			if (missingJewels == 0) {
				si.Add (inputAllLeafletsDone, CurrentAgent.Parameters.MAX_ACTIVATION);
			}

			//Console.WriteLine(sensorialInformation);

			int n = 0;
			foreach(Leaflet l in c.getLeaflets()) {
				mind.updateLeaflet(n,l);
				n++;
			}

            return si;
        }
        #endregion

        #region Fixed Rules
        private double FixedRuleToAvoidCollisionWall(ActivationCollection currentInput, Rule target)
        {
            // See partial match threshold to verify what are the rules available for action selection
            return ((currentInput.Contains(inputWallAhead, CurrentAgent.Parameters.MAX_ACTIVATION))) ? 1.0 : 0.0;
        }

        private double FixedRuleToGoAhead(ActivationCollection currentInput, Rule target)
        {
            // Here we will make the logic to go ahead
            return ((currentInput.Contains(inputWallAhead, CurrentAgent.Parameters.MIN_ACTIVATION))) ? 1.0 : 0.0;
        }

		private double FixedRuleToGetJewel(ActivationCollection currentInput, Rule target)
		{
			// Here we will make the logic to collect a jewel
			return ((currentInput.Contains(inputJewelAhead, CurrentAgent.Parameters.MAX_ACTIVATION))) ? 1.0 : 0.0;
		}

		private double FixedRuleToStop(ActivationCollection currentInput, Rule target)
		{
			// Here we will make the logic to stop after collecting all jewels.
			return ((currentInput.Contains(inputAllLeafletsDone, CurrentAgent.Parameters.MAX_ACTIVATION))) ? 1.0 : 0.0;
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
