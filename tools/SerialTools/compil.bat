@echo off
echo Compiling
mkdir bin
del /s /q bin\*
javac -d bin -cp ./src;./libs/jssc.jar src/BdosSerialSender.java
echo done
