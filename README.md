# FactorBase
[![Build Status](https://travis-ci.org/sfu-cl-lab/FactorBase.svg?branch=master)](https://travis-ci.org/sfu-cl-lab/FactorBase)   
The source code repository for the FactorBase system. 
The code in this repository implements the learn-and-join algorithm (see [algorithm paper](http://www.cs.sfu.ca/~oschulte/pubs.html) on  ''Learning Graphical Models for Relational Data via Lattice Search''). 

_Input_: A relational schema hosted on a MySQL server. 

_Output_: A Bayesian network that shows probabilistic dependencies between the relationships and attributes represented in the database. Both network structure and parameters are computed by the system.

## Further Information

+ Our [project website](https://sfu-cl-lab.github.io/FactorBase/) contains various helpful information such as pointers to datasets, a gallery of learned models, and system tips. 
+ The [tutorial](https://oschulte.github.io/srl-tutorial-slides/) explains the concepts underlying the algorithm and our other tools 
+ Our [system paper](http://www.cs.sfu.ca/~oschulte/pubs.html) titled '' FactorBase: Multi-Relational Model Learning with SQL All The Way'' explains the system components.

## How to Use - Overview

1. **Import data into a Mysql server**

	We provide two sets of example datasets in `testsql` folder. These are `.sql` files for:
	* Mutagenesis
	* Uneilwin : Dataset about the following schema ![University Schema](/images/univschema.png)
	
2. **Install the program** 

	Navigate to the folder where you can find FactorBase.jar by 

	` cd <pathToFactorBaseDirectory>/FactorBase/jar ` 

	Optional: You can also make a local copy of FactorBase.jar in ```/usr/bin/jar```.

3. **Point to the required database in your MySQL server** 

	Modify `jar/config.cfg` with your own configuration according to the sample format explained in the image. ![Sample Configuration](/images/configuration.png).  

4. **Learn a Bayesian network structure** 

	In the `FactorBase/jar` folder, run 
		
	`java -jar FactorBase.jar`
		
	**Note**: For big databases, you may need to specify larger java heap size by
	
	`java -jar -Xmx8G FactorBase.jar`

5. **Inspect the Bayesian Network (BN)**

	We follow the [BayesStore](http://dl.acm.org/citation.cfm?id=1453896) design philosphy where statistical objects are treated as managed within the database. 
	
	1. The network structure is stored in `<db_BN>.Final_Path_BayesNets` where `<db_BN>` is the model database specified in your configuration file.
	2. The conditional probability tables are stored in tables named `<db_BN.nodename>_CP` where `nodename` is the name of the child node.

## Other Output Formats: BIF, MLN, ETL

The learned BN structure can be exported from the database to support a number of other applications.

+ **[BIF_Generator](https://github.com/sfu-cl-lab/BIF_Generator)**
	* Bayesian Interchange Format (BIF) Generator produces an .xml file that can be loaded into a standard **Bayesian Network** tool (like [AIspace tool](http://aispace.org/bayes/).) 
	*This is the best way to visualize the learned graph structure.* 
	* Queries in the learned Bayesian networks can be used as a Statistical-Relational Model to estimate frequencies in the database as explained [here](https://www.researchgate.net/publication/2919745_Selectivity_Estimation_using_Probabilistic_Models) and in our paper on [Modelling Relational Statistics With Bayes Nets](http://www.cs.sfu.ca/%7Eoschulte/files/pubs/ilp2012.pdf). 

+ **[MLN_Generator](https://github.com/sfu-cl-lab/MLN_Generator)**
	* Markov Logic Network (MLN) is a first-order knowledge base with a weight attached to each formula (or clause)
	* Convert the learned BN into MLN by running `java -jar MLNExporter.jar `. For more details see .

+ **[Extract, Transform, Load](https://en.wikipedia.org/wiki/Extract,_transform,_load)**
	* The learned BN structure defines a set of features that can be used to transform the information in relational data into a single table format. The single table can then be loaded into standard machine learning tools. In the relational learning literature, this process is called [Propositionalization](http://link.springer.com/referenceworkentry/10.1007%2F978-0-387-30164-8_680). See also the [tutorial on Relational Bayes Net Classifier](https://oschulte.github.io/srl-tutorial-slides/ch5-rel-bayes-net-classifier.pptx).

	+ [Classification](https://github.com/sfu-cl-lab/etl-classification). Given a target predicate (DB column), this tool produces a single-table data with relational features. 
	
	+ [Outlier detection](https://github.com/sfu-cl-lab/etl-outlier-detection). Given a target entity class, this tool produces a single-table data with relational features. 


## Model-based Tools for Other Applications

After running the learn-and-join algorithm, the learned Bayesian network can be leveraged for various applications. 

+ [Relational Classification](https://github.com/sfu-cl-lab/relational-classification). Given a tarN by
`java -jar MLNExporter.jar`.
For more details, see MLN_Generator. Get instance (cell in the database), compute a probability for each possible value.

+ [Data Cleaning](https://github.com/sfu-cl-lab/data-cleaning) 
Given a relational database, rank the database values according to their (im)probability.

+ [Exception Mining](https://github.com/sfu-cl-lab/exception-mining) 
Given a relational database and a target entity set, rank each entity according to how exceptional it is within its class. This tool implements our expected **log-distance metric** from our paper [Model-based Outlier Detection for Object-Relational Data](http://www.cs.sfu.ca/~oschulte/pubs.html). Our approach fits within the general framework of [exceptional model mining](http://www.cs.uu.nl/groups/ADA/emm/), also see the [tutorial on Anomaly Detection](https://oschulte.github.io/srl-tutorial-slides/ch6-anomaly.pptx). 

  
### Compile & Run  
+ Go into `src` folder 
+ modify `config.cfg`  with your own configuration according to format explained [here]
+ `javac -cp ".:./lib/*" Config.java BZScriptRunner.java MakeSetup.java`  
+ `javac -cp ".:./lib/*" RunBB.java`  
+ `java -cp ".:./lib/*" MakeSetup`  
+ `java -cp ".:./lib/*" RunBB`  
+ Optionally set up the target database and run FunctorWrapper  
  + `javac -cp ".:./lib/*" MakeTargetSetup.java`  
  + `javac -cp ".:./lib/*" FunctorWrapper.java`  
  + `java -cp ".:./lib/*" MakeTargetSetup`  
  + `java -cp ".:./lib/*" FunctorWrapper` 

