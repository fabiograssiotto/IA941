
using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Threading;
using System.IO;
using ClarionApp;
using ClarionApp.Model;
using ClarionApp.Exceptions;
using Clarion;
using Clarion.Framework;
using Gtk;
using System.Threading.Tasks;

namespace ClarionApp
{
	class MainClass
	{
		#region properties
		private WSProxy ws = null;
        private ClarionAgent agent;
        String creatureId = String.Empty;
        String creatureName = String.Empty;
		#endregion

		#region constructor
		public MainClass() {
			Application.Init();
			Console.WriteLine ("ClarionApp V0.8");
			try
            {
                ws = new WSProxy("localhost", 4011);

                String message = ws.Connect();

                if (ws != null && ws.IsConnected)
                {
                    Console.Out.WriteLine("[SUCCESS] " + message + "\n");
                    ws.SendWorldReset();
                    ws.NewCreature(400, 200, 0, out creatureId, out creatureName);
                    
                    ws.SendCreateLeaflet();
                    //ws.NewBrick(4, 799, 1, 800, 600);
                    //ws.NewBrick(4, 50, -4, 747, 47);
                    //ws.NewBrick(4, 49, 562, 796, 599);
                    //ws.NewBrick(4, -2, 6, 50, 599);

                    // Create entities continuously.
                    Task.Delay(0).ContinueWith(t => CreateEntities());

                    if (!String.IsNullOrWhiteSpace(creatureId))
                    {
                        ws.SendStartCamera(creatureId);
                        ws.SendStartCreature(creatureId);
                    }

                    Console.Out.WriteLine("Creature created with name: " + creatureId + "\n");
                    agent = new ClarionAgent(ws, creatureId, creatureName);
                    agent.Run();
                    Console.Out.WriteLine("Running Simulation ...\n");
                }
                else {
					Console.Out.WriteLine("The WorldServer3D engine was not found ! You must start WorldServer3D before running this application !");
					System.Environment.Exit(1);
				}
            }
            catch (WorldServerInvalidArgument invalidArtgument)
            {
                Console.Out.WriteLine(String.Format("[ERROR] Invalid Argument: {0}\n", invalidArtgument.Message));
            }
            catch (WorldServerConnectionError serverError)
            {
                Console.Out.WriteLine(String.Format("[ERROR] Is is not possible to connect to server: {0}\n", serverError.Message));
            }
            catch (Exception ex)
            {
                Console.Out.WriteLine(String.Format("[ERROR] Unknown Error: {0}\n", ex.Message));
            }
			Application.Run();
		}

        private void CreateEntities()
        {
            // Create a set of jewels in the environment
            Random rnd = new Random();
            for (int i = 0; i < 20; i++)
            {
                // Randomly select color and position.
                int color = rnd.Next(0, 6);
                int x = rnd.Next(100, 800);
                int y = rnd.Next(100, 600);
                ws.NewJewel(color, x, y);
            }

            // Create a set of 10 food items in the environment
            for (int i = 0; i < 10; i++)
            {
                int food = rnd.Next(0, 1);
                int x = rnd.Next(100, 800);
                int y = rnd.Next(100, 600);
                ws.NewFood(food, x, y);
            }

            Task.Delay(30000).ContinueWith(t => CreateEntities());
        }

        #endregion

        #region Methods
        public static void Main(string[] args)
        {
            if (!IsUnixBased)      //for Windows Run-Time Engines only..
                CheckWindowsGtk(); // Fixes GTK library find issues

            new MainClass();
        }

        /// <summary>
        /// Detects if the Running Environment is Unix-based.
        /// </summary>
        /// <returns>True if execution environment is unix-based.</returns>
        /// Solution found on:
        /// <see cref="http://www.mono-project.com/docs/faq/technical/"/>
        static bool IsUnixBased {
            get {
                int p = (int)Environment.OSVersion.Platform;
                return (p == 4) || (p == 6) || (p == 128);
            }
        }

        /// <summary>
        /// Solve DLL path issues on Windows regarding GTK# v2.12.22+
        /// </summary>
        /// <returns>True if solved the path problem. False, otherwise.</returns>
        /// Solution found on:
        /// <see cref="https://github.com/picoe/Eto/issues/442"/>
        static bool CheckWindowsGtk()
        {
            string location = null;
            Version version = null;
            Version minVersion = new Version(2, 12, 22);
            using (var key = Microsoft.Win32.Registry.LocalMachine.OpenSubKey(@"SOFTWARE\WOW6432Node\Xamarin\GtkSharp\InstallFolder"))
            {
                if (key != null)
                    location = key.GetValue(null) as string;
            }
            using (var key = Microsoft.Win32.Registry.LocalMachine.OpenSubKey(@"SOFTWARE\WOW6432Node\Xamarin\GtkSharp\Version"))
            {
                if (key != null)
                    Version.TryParse(key.GetValue(null) as string, out version);
            }
            //TODO: check build version of GTK# dlls in GAC
            if (version == null || version < minVersion || location == null || !File.Exists(Path.Combine(location, "bin", "libgtk-win32-2.0-0.dll")))
            {
                Console.WriteLine("Did not find required GTK# installation");
                //  string url = "http://monodevelop.com/Download";
                //  string caption = "Fatal Error";
                //  string message =
                //      "{0} did not find the required version of GTK#. Please click OK to open the download page, where " +
                //      "you can download and install the latest version.";
                //  if (DisplayWindowsOkCancelMessage (
                //      string.Format (message, BrandingService.ApplicationName, url), caption)
                //  ) {
                //      Process.Start (url);
                //  }
                return false;
            }
            Console.WriteLine("Found GTK# version " + version);
            var path = Path.Combine(location, @"bin");
            Console.WriteLine("SetDllDirectory(\"{0}\") ", path);
            try
            {
                if (SetDllDirectory(path))
                {
                    return true;
                }
            }
            catch (EntryPointNotFoundException)
            {
            }
            //
            // this shouldn't happen unless something is weird in Windows
            Console.WriteLine("Unable to set GTK+ dll directory");
            return true;
        }

        [System.Runtime.InteropServices.DllImport("kernel32.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode, SetLastError = true)]
        [return: System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.Bool)]
        static extern bool SetDllDirectory(string lpPathName);  //must not change this method's signature..declared in kernel32.dll..

        #endregion
    }


}
