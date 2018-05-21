#!/bin/bash
# Script to launch WordServer3d
java -jar ws3d/WorldServer3D.jar &
sleep 5
# Script to launch ClarionApp
mono DemoClarion\ClarionApp\bin\Release\ClarionApp.exe &
# Script to launch DemoSoar
java -jar DemoJSOAR\DemoJSOAR.jar &
