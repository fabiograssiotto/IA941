@echo off
rem Script to launch WordServer3d
start java -jar ws3d\WorldServer3D.jar
sleep 5
rem Script to launch ClarionApp
start DemoClarion\ClarionApp\bin\Release\ClarionApp.exe
