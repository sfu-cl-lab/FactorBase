# Contingency Table Generator for Relational Data

Implements an efficient SQL-based method for computing instantiation counts for conjunctive conditions in a relational database. The method is described in our [algorithm paper](http://www.cs.sfu.ca/~oschulte/files/pubs/Qian2014.pdf). See [publication list](http://www.cs.sfu.ca/~oschulte/pubs.html) for a brief summary.

### Input

+ A relational database `datadb`
+ A set of first-order terms _t<sub>1</sub>,..,t<sub>n</sub>_. Default: complete, contains all terms associated with the relational schema.
+ Optional Expansion: A set of first-order variables _A<sub>1</sub>,...,A<sub>e</sub>,_. Default: empty.
+ Optional Grounding: A set of groundings first-order variable _A<sub>1</sub>=a<sub>1</sub>,...,_A<sub>g</sub>=a<sub>g</sub>. Default: empty.

### Output

A contingency table of the form

count | t<sub>1</sub> | ... | t<sub>n</sub>|(A<sub>1</sub>)|...|(A<sub>e</sub>)|
-----| ---------------|-----|--------------|---------------|----|---------------|
integer| value<sub>1</sub>|....| value<sub>n</sub>|_a<sub>1</sub>_|....|_a<sub>e</sub>_|

where

+ the values in each row define a conjunctive query _t<sub>1</sub>=value<sub>1</sub>,..,t<sub>n</sub>=value<sub>n</sub>_
+ `count` is the number of times that the query is instantiated in the database `data_db` (i.e. the size of the query's result set).
+ If groundings of the form _A = a_ are specified, the system adds each condition _A = a_ to the query; the contingency table represents the query instantiation counts for the individuals named _a_ only.
+ If first-order variables are specified, then _a_ is a constant denoting an individual from the domain of each give first-order variable _A_. The contingency table represents the query instantiation counts for each tuple (_a<sub>1</sub>_,_a<sub>e</sub>_) of individuals in the respective domains of (A<sub>1</sub>,...,A<sub>e</sub>).

## Usage

1. Specify the input database `datadb`: Modify `jar/config.cfg` with your own configuration as explained in the [repository readme](https://github.com/sfu-cl-lab/FactorBase/blob/master/README.md). See our [project website](https://sfu-cl-lab.github.io/FactorBase/options.html) for an explanation of the options. 
2. Run `MakeSetup.runMS()`. This creates a database named `datadb_setup` containing metadata. Edit the following tables (using SQL).
   + `FunctorSet` contains a list of all first-order terms for the database (called Fnodes). Delete all terms that should _not_ be in the contingency table.
   + `Expansions` is empty by default. Insert first-order variables for expansions. The table `Pvariables` lists the available first-order variables (called population variables).
   + `Groundings` is empty by default. Insert first-order variables and constants for groundings. The table `Pvariables` lists the available first-order variables (called population variables).
 3. Run `BayesBaseCT_SortMerge.buildCT()`. This writes the contingency table to a database called `datadb_ct`.
