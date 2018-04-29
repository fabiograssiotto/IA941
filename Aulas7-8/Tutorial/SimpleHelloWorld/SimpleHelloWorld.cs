using System;
using System.Diagnostics;
using System.IO;
using Clarion;
using Clarion.Framework;

namespace SimpleHelloWorld
{
	public class SimpleHelloWorld
	{
		public SimpleHelloWorld ()
		{
		}

		static void Main(string[] args)
		{
			Console.WriteLine ("Initializing the Simple Hellow World Task");

			int correctCounter = 0;
			int numberTrials = 10000;
			int progress = 0;

			World.LoggingLevel = TraceLevel.Warning;

			TextWriter orig = Console.Out;
			StreamWriter sw = File.CreateText ("HelloWorldSimple.txt");

		}
			
	}
}

