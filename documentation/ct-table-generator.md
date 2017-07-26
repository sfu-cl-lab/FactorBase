# Contingency Table Generator for Relational Data

Implements an efficient SQL-based method for computing instantiation counts for conjunctive conditions in a relational database. The method is described in our [algorithm paper](http://www.cs.sfu.ca/~oschulte/files/pubs/Qian2014.pdf). See [publication list](http://www.cs.sfu.ca/~oschulte/pubs.html) for a brief summary.

### Input

+ A relational database `data_db`
+ A set of first-order terms _t<sub>1</sub>,..,t<sub>n</sub>_. Default: all terms associated with the relational schema.
+ Optional Expansion: A first-order variable _A_. Default: unspecified.
+ Optional Grounding: A first-order variable _A_ and a constant _a_. Default: unspecified.

### Output

A contingency table of the form

count | t<sub>1</sub> | ... | t<sub>n</sub>|(A_id)|
-----| ---------------|-----|--------------|-------|
integer| value<sub>1</sub>|....| value<sub>n</sub>|_a_|

where

+ the values in each row define a conjunctive query _t<sub>1</sub>=value<sub>1</sub>,..,t<sub>n</sub>=value<sub>n</sub>_
+ `count` is the number of times that the query is instantiated in the database `data_db` (i.e. the size of the query's result set).
+ If a grounding _A = a_ is specified, the system adds the condition _A = a_ to the query; the contingency table represents the query instantiation counts for the individual _a_ only.
+ If a first-order variable is specified, then _a_ is a constant denoting an individual from the domain of the first-order variable _A_. The contingency table represents the query instantiation counts for each individual in the domain of _A_.

## Usage

Modify `jar/config.cfg` with your own configuration according to the sample format explained in the image. ![Sample Configuration](/images/configuration.png). See our [project website](https://sfu-cl-lab.github.io/FactorBase/options.html) for an explanation of the options.
