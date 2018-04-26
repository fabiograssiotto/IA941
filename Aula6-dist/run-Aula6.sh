#!/bin/bash
# Script to launch WordServer3d and DemoJSOAR
java -jar ws3d/WorldServer3D.jar &
sleep 5
java -jar DemoJSOAR/DemoJSOAR.jar 
