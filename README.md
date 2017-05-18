# FactorBase
[![Build Status](https://travis-ci.org/sfu-cl-lab/FactorBase.svg?branch=master)](https://travis-ci.org/sfu-cl-lab/FactorBase)   
The source code repository for the FactorBase system. The input to the system is a relational schema hosted on a MySQL. The output is a Bayes net that shows probabilistic dependencies between the relationships and attributes represented in the database. 
The code in this repository implements the learn-and-join algorithm (see [algorithm paper](http://www.cs.sfu.ca/~oschulte/pubs.html) on  ''Learning Graphical Models for Relational Data via Lattice Search''). 

## Further Information

+ Our [project website](http://www.cs.sfu.ca/~oschulte/BayesBase/BayesBase.html) contains various helpful information such as pointers to datasets, a gallery of learned models, and system tips. 
+ The [tutorial] explains the concepts underlying the algorithm and our other tools (https://oschulte.github.io/srl-tutorial-slides/)
+  Our [system paper](http://www.cs.sfu.ca/~oschulte/pubs.html) titled '' FactorBase: Multi-Relational Model Learning with SQL All The Way'' explains the system components.

## How to Use - Overview

1. Import data into a Mysql server. We provide two sets of example datasets in `testsql` folder. 
2. Install the program: Make a local copy of `jar/FactorBase.jar` in a `jar` directory. You can also recompile the `FactorBase.jar` using our source code. (More details below.) 
3. Point the program to your MySQL server: Modify `jar/config.cfg` with your own configuration according to the format explained [here](http://www.cs.sfu.ca/~oschulte/BayesBase/options.html).  
4. Learn a Bayesian network structure:  In the `jar` folder, run `java -jar FactorBase.jar`. 
	+ For big databases, you may need to specify larger java heap size. For example: `java -jar -Xmx8G FactorBase.jar`.  
5. Inspect the Bayesian network. We follow the [BayesStore](http://dl.acm.org/citation.cfm?id=1453896) design philophy where statistical objects are treated as managed within the database. 
	1. The network structure is stored in `<db_BN>.Final_Path_BayesNets` where <db_BN> is the model database specified in your configuration file.
	2. The conditional probability tables are stored in tables named `<db_BN.nodename>_CP` where `nodename` is the name of the child node.

## Other Output Formats

The learned BN structure can be exported from the database to support a number of other applications.

+ BIF format: Run the [BIF_Generator](https://github.com/sfu-cl-lab/BIF_Generator). This produces an .xml file that can be loaded into a standard Bayesian network tool. (We have tested it with the [AIspace tool](http://aispace.org/bayes/).) Queries in the learned Bayesian networks can be used as a Statistical-Relational Model to estimate frequencies in the database as explained [here](https://www.researchgate.net/publication/2919745_Selectivity_Estimation_using_Probabilistic_Models) and in our paper on [Modelling Relational Statistics With Bayes Nets](http://www.cs.sfu.ca/~oschulte/pubs.html). 

+ Markov Logic Network. Convert the learned BN into MLN by running `java -jar MLNExporter.jar `. For more details see [MLN_Generator](https://github.com/sfu-cl-lab/MLN_Generator).

+ [Extract, Transform, Load](https://en.wikipedia.org/wiki/Extract,_transform,_load). The learned BN structure defines a set of features that can be used to transform the information in relational data into a single table format. The single table can then be loaded into standard machine learning tools. In the relational learning literature, this process is called [Propositionalization](http://link.springer.com/referenceworkentry/10.1007%2F978-0-387-30164-8_680). 

	+[Classification](https://github.com/sfu-cl-lab/etl-classification). Given a target predicate (DB column), this tool produces a single-table data with relational features. 
	
	+[Outlier detection](https://github.com/sfu-cl-lab/etl-outlier-detection). Given a target entity class, this tool produces a single-table data with relational features. 


## Model-based Tools for Other Applications

After running the learn-and-join algorithm, the learned Bayesian network can be leveraged for various applications. 

+[Relational Classification](https://github.com/sfu-cl-lab/relational-classification). Given a target instance (cell in the database), computes a probability for each possible value.

+[Data Cleaning](https://github.com/sfu-cl-lab/data-cleaning) Given a relational database, ranks the database values according to their (im)probability.

+[Exception Mining](https://github.com/sfu-cl-lab/exception-mining) Given a relational database and a target entity set, ranks each entity according to how exceptional it is within its class. Implements our expected log-distance metric from our paper [
Model-based Outlier Detection for Object-Relational Data] (http://www.cs.sfu.ca/~oschulte/pubs.html). Our approach fits within the general framework of [exceptional model mining](http://www.cs.uu.nl/groups/ADA/emm/), see also the [tutorial](https://oschulte.github.io/srl-tutorial-slides/ch6-anomaly.pptx). 



## How to Use  
First you should import data into your database. We provide two sets of example datasets in `testsql` folder. Then you can either run `.jar` or compile the source yourself. If you want to visualize the BayesNet learned, you can run [BIF_Generator](https://github.com/sfu-cl-lab/BIF_Generator)  
### Run .jar to get the BN and MLN 
1. Modify `jar/config.cfg` with your own configuration according to format explained [here](http://www.cs.sfu.ca/~oschulte/BayesBase/options.html)  
2. In `jar` folder, run `java -jar FactorBase.jar`  
+ Converting the learned BN into MLN by running `java -jar MLNExporter.jar `
+ For big databases, you need to specify larger java heap size. For example: `java -jar -Xmx8G FactorBase.jar`   
  
### Compile & Run  
+ Go into `src` folder 
+ modify `config.cfg`  with your own configuration according to format explained [here]
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
