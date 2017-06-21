#!/bin/bash
cd src/main/java
javac -cp ".:./lib/*" Config.java BZScriptRunner.java MakeSetup.java
javac -cp ".:./lib/*" RunBB.java
java -cp ".:./lib/*" RunBB

# Clean the .class files in java folder
rm *.class

#Back to home
cd ../../../

