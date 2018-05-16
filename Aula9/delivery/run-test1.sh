#!/bin/bash
# Script to launch WordServer3d
java -jar ws3d/WorldServer3D.jar &
sleep 5
# Script to launch ClarionApp
mono DemoClarion/Linux/ClarionApp.exe &
