@echo off
rem Script to launch WordServer3d
start java -jar ws3d\WorldServer3D.jar
sleep 5
rem Script to launch DemoSoar
start java -jar DemoJSOAR\DemoJSOAR.jar
sleep 5
rem Script to launch ClarionApp
start DemoClarion\Win\ClarionApp.exe vs

