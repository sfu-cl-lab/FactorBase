#！ /bin/bash
set -o pipefail

preDatabse="unielwin"

cd ~/FactorBase/testScripts/inputs

for database in `ls`
do
	cd ~/FactorBase/code/factorbase
	echo ${database}
	gsed -i "s/${preDatabse}/${database}/g" ../../travis-resources/config.cfg
	java -Dconfig=../../travis-resources/config.cfg -jar target/factorbase-1.0-SNAPSHOT.jar >${database}_log.txt
	preDatabse=${database}
	done