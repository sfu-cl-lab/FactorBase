# FactorBase
[![Build Status](https://travis-ci.org/sfu-cl-lab/FactorBase.svg?branch=master)](https://travis-ci.org/sfu-cl-lab/FactorBase)   
The source code repository for the Factor Base system. The flat learner of the code are classes for CMU's Tetrad system. We may also add datasets if we get around to it.  
For more information about this project, visit our [project website](http://www.cs.sfu.ca/~oschulte/BayesBase/BayesBase.html)  
##How to Use  
First you should import data into your database. We provide two sets of example datasets in `testsql` folder. Then you can either run `.jar` or compile the source yourself. If you want to visualize the BayesNet learned, you can run [BIF_Generator](https://github.com/sfu-cl-lab/BIF_Generator)  
###Run .jar  
+ Modify `jar/cfg/subsetctcomputation.cfg` with your own configuration according to format explained [here](http://www.cs.sfu.ca/~oschulte/BayesBase/options.html)  
+ In `jar` folder, run `java -jar FactorBase.jar`  
+ For big databases, you need to specify larger java heap size. For example: `java -jar -Xmx8G FactorBase.jar`   
  
###Compile & Run  
+ Go into `src` folder and modify `config.cfg`  
+ `javac -cp ".:./lib/*" Config.java BZScriptRunner.java MakeSetup.java`  
+ `javac -cp ".:./lib/*" RunBB.java`  
+ `mkdir src`  
+ `mv scripts src/`  
+ `java -cp ".:./lib/*" MakeSetup`  
+ `java -cp ".:./lib/*" RunBB`  
+ Optionally set up the target database and run FunctorWrapper  
  + `javac -cp ".:./lib/*" MakeTargetSetup.java`  
  + `javac -cp ".:./lib/*" FunctorWrapper.java`  
  + `java -cp ".:./lib/*" MakeTargetSetup`  
  + `java -cp ".:./lib/*" FunctorWrapper` 
  
## Project Specification  
Please visit our [project website](http://www.cs.sfu.ca/~oschulte/BayesBase/BayesBase.html)
