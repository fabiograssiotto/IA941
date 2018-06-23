#!/bin/bash
# Script to launch WordServer3d
java -jar ws3d/WorldServer3D.jar &
sleep 5
# Script to launch CST-A2
java -jar CST-A2/CST-A2.jar &
