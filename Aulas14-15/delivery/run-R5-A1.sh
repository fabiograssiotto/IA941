#!/bin/bash
# Script to launch WordServer3d
java -jar ws3d/WorldServer3D.jar &
sleep 5
# Script to launch CST-A1
java -jar CST-A1/CST-A1.jar &
