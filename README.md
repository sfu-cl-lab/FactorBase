# FactorBase: Learning Graphical Models from multi-relational data

[![Build Status](https://travis-ci.org/sfu-cl-lab/FactorBase.svg?branch=master)](https://travis-ci.org/sfu-cl-lab/FactorBase)   
The source code repository for the FactorBase system. 
The code in this repository implements the learn-and-join algorithm (see [algorithm paper](http://www.cs.sfu.ca/~oschulte/pubs.html) on  ''Learning Graphical Models for Relational Data via Lattice Search''). 


+ _Input_: A relational schema hosted on a MySQL server. 

+ _Output_: A Bayesian network that shows probabilistic dependencies between the relationships and attributes represented in the database. Both network structure and parameters are computed by the system.

## Contingency Table Generator

One of the key computational problems in relational learning and inference is to compute how many times a conjunctive condition is instantiated in a relational structure. FactorBase computes _relational contingency tables_, which store for a given set of first-order terms/predicates how many times different value combinations of the terms are instantiated in the input database. Given the general importance of this problem in pretty much any relational data problem, we provide [stand-alone code](https://github.com/sfu-cl-lab/FactorBase/blob/master/documentation/ct-table-generator.md) for computing contingency tables that can be  used independently of our Bayesian network learning system.


## Further Information

+ Our [project website](https://sfu-cl-lab.github.io/FactorBase/) contains various helpful information such as pointers to datasets, a gallery of learned models, and system tips. 
+ The [tutorial](https://oschulte.github.io/srl-tutorial-slides/) explains the concepts underlying the algorithm and our other tools 
+ Our [system paper](http://www.cs.sfu.ca/~oschulte/pubs.html) titled '' FactorBase: Multi-Relational Model Learning with SQL All The Way'' explains the system components.

-------------------------------------

## How to Use - Overview

1. **Import data into a Mysql server**

	We provide two sets of example datasets in `testsql` folder. These are `.sql` files for:
	* Mutagenesis
	* Uneilwin : Dataset about the following schema ![University Schema](/images/univschema.png)

2. **Install the program** 

	First clone the project by running a command similar to the following:

	```shell
	git clone https://github.com/sfu-cl-lab/FactorBase.git
	```

	FactorBase and other tools in the project can all be built using the following command (make sure to have [Maven](https://maven.apache.org) installed):

	```shell
	cd FactorBase/code
	mvn install
	```

	After the above commands are successfully run, an executable JAR file for FactorBase can be found at:

	```shell
	factorbase/target/factorbase-<version>-SNAPSHOT.jar
	```
	Where the `<version>` field is the version of FactorBase that you have generated.

3.  **Update `config.cfg`  with your own configuration according to format explained [here](https://sfu-cl-lab.github.io/FactorBase/options.html)**

4. **Point to the required database in your MySQL server**

	Modify `travis-resources/config.cfg` with your own configuration according to the sample format explained in the image.
	
	 ![Sample Configuration](/images/configuration.png).

	See our [project website](https://sfu-cl-lab.github.io/FactorBase/options.html) for an explanation of the options.

	For the last row, you can set the global logger to this threee levels:
	- debug: show all log messages;
	- info: only show info, warning and error messages(no debug message), which is the default;
	- off: show no log message;

5. **Learn a Bayesian Network Structure**

	In the `FactorBase` folder, run

	```shell
	java -jar factorbase/target/factorbase-<version>-SNAPSHOT.jar
	```

	Where the `<version>` field is the version of FactorBase that you have generated.

	**Note**: For big databases, you may need to specify larger java heap size by

	```shell
	java -jar -Xmx8G factorbase/target/factorbase-<version>-SNAPSHOT.jar
	```

	By default the executable JAR file will look for the configuration file in the current directory (i.e. where you are running the command), if you would like to specify a different configuration file to use when running FactorBase you can use the parameter `-Dconfig=<config-file>`.  For example:

	```shell
	java -Dconfig=../travis-resources/config.cfg -jar factorbase/target/factorbase-<version>-SNAPSHOT.jar
	```

6. **Inspect the Bayesian Network (BN)**

	We follow the [BayesStore](http://dl.acm.org/citation.cfm?id=1453896) design philosphy where statistical objects are treated as managed within the database. 

	1. The network structure is stored in the table `Final_Path_BayesNets` of the `<db>_BN` database where `<db>` is the model database specified in your configuration file.
	2. The conditional probability tables are stored in tables named `<nodename>_CP` of the `<db>_BN` database where `<db>` is the model database specified in your configuration file and `<nodename>` is the name of the child node.

===============
## Other Output Formats: BIF, MLN, ETL

The learned BN structure can be exported from the database to support a number of other applications.

+ **[BIF_Generator](https://github.com/sfu-cl-lab/BIF_Generator)**
	* Bayesian Interchange Format (BIF) Generator produces an .xml file that can be loaded into a standard **Bayesian Network** tool (like [AIspace tool](http://aispace.org/bayes/).) 
	*This is the *best* way to visualize the learned graph structure.* 
	* Queries in the learned Bayesian networks can be used as a Statistical-Relational Model to estimate frequencies in the database as explained [here](https://www.researchgate.net/publication/2919745_Selectivity_Estimation_using_Probabilistic_Models) and in our paper on [Modelling Relational Statistics With Bayes Nets](http://www.cs.sfu.ca/%7Eoschulte/files/pubs/ilp2012.pdf). 
	* The table shows the bayesian network xml files learned from some datasets. The sql file is the MySQL dump for the relation schema, while the output is the bayesian network in BIF/XML format.

	| datasets      	      | sql                         | BIF/XML                   |
	| :-------------: 	      |:-------------:              |  :-----:                   |
	| unielwin | [unielwin.sql](./testsql/unielwin) | [Bif_unielwin.xml](./BN_xml/Bif_unielwin.xml) | 
	| Mutagenesis_std | [Mutagenesis_std.sql](./testsql/Mutagenesis_std) | [Bif_Mutagenesis_std.xml](./BN_xml/Bif_Mutagenesis_std.xml) |
	| MovieLens_std | [MovieLens_std.sql](./testsql/MovieLens_std) | [Bif_MovieLens_std.xml](./BN_xml/Bif_MovieLens_std.xml) |


+ **[MLN_Generator](https://github.com/sfu-cl-lab/MLN_Generator)**
	* Markov Logic Network (MLN) is a first-order knowledge base with a weight attached to each formula (or clause)
	* Convert the learned BN into MLN by running `java -jar MLNExporter.jar `. For more details see .

+ **[Extract, Transform, Load](https://en.wikipedia.org/wiki/Extract,_transform,_load)**
	* The learned BN structure defines a set of features that can be used to transform the information in relational data into a single table format. The single table can then be loaded into standard machine learning tools. In the relational learning literature, this process is called [Propositionalization](http://link.springer.com/referenceworkentry/10.1007%2F978-0-387-30164-8_680). See also the [tutorial on Relational Bayes Net Classifier](https://oschulte.github.io/srl-tutorial-slides/ch5-rel-bayes-net-classifier.pptx).

	+ [Feature Generation for Classification](https://github.com/sfu-cl-lab/etl-classification). Given a target predicate (DB column), this tool produces a single-table data with relational features. 

	+ [Feature Generation for Outlier detection](https://github.com/sfu-cl-lab/etl-outlier-detection). Given a target entity table/class, this tool produces a single-table data with relational features. 

-------------------------------------------------

## Other Applications (May Be Under Construction) 

After running the learn-and-join algorithm, the learned Bayesian network can be leveraged for various applications. 

+ [Relational Classification](https://github.com/sfu-cl-lab/relational-classification). Given a tarN by
`java -jar MLNExporter.jar`.
For more details, see MLN_Generator. Get instance (cell in the database), compute a probability for each possible value.

+ [Data Cleaning](https://github.com/sfu-cl-lab/data-cleaning) 
Given a relational database, rank the database values according to their (im)probability.

+ [Exception Mining](https://github.com/sfu-cl-lab/exception-mining) 
Given a relational database and a target entity set, rank each entity according to how exceptional it is within its class. This tool implements our expected **log-distance metric** from our paper [Model-based Outlier Detection for Object-Relational Data](http://www.cs.sfu.ca/~oschulte/pubs.html). Our approach fits within the general framework of [exceptional model mining](http://www.cs.uu.nl/groups/ADA/emm/), also see the [tutorial on Anomaly Detection](https://oschulte.github.io/srl-tutorial-slides/ch6-anomaly.pptx).