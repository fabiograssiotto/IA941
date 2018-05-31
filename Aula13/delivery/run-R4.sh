#!/bin/bash
# Script to launch WordServer3d
java -jar ws3d/WorldServer3D.jar &
# Script to launch DemoLIDA
cd DemoLIDA
java -jar DemoLIDA.jar &
