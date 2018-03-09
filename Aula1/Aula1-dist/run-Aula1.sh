#!/bin/bash
# Script to launch WordServer3d and ManualController.
java -jar ws3d/WorldServer3D.jar &
sleep 2
java -jar ManualController/ManualController.jar 
