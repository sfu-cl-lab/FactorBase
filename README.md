# BayesBase
The source code repository for the Bayes Base system.  Most of the code are classes for CMU's Tetrad system. We may also add datasets if we get around to it.  
For more information about this project, visit our [project website](http://www.cs.sfu.ca/~oschulte/BayesBase/BayesBase.html)  
##How to Use  
###Run .jar  
+ Modify `jar/src/config.cfg` with your own configuration according to format explained [here](http://www.cs.sfu.ca/~oschulte/BayesBase/options.html)  
+ In `jar` folder, run `java -jar RunBB.jar`  
###Compile & Run  
+ Go into `src` folder and modify `config.cfg`  
+ `javac -cp ".:./lib/*" Config.java BZScriptRunner.java MakeSetup.java`  
+ `java -cp ".:./lib/*" MakeSetup`  
+ `javac -cp ".:./lib/*" RunBB.java`  
+ `java -cp ".:./lib/*" RunBB`  
+ `javac -cp ".:./lib/*" MakeTargetSetup.java`  
+ `java -cp ".:./lib/*" MakeTargetSetup`  
+ `javac -cp ".:./lib/*" FunctorWrapper.java`  
+ `java -cp ".:./lib/*" FunctorWrapper`  
