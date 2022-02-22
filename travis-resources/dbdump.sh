#!/bin/bash
set -o pipefail

CLEAR='\033[0m'
GREEN='\033[0;32m'
RED='\033[0;31m'

output="mysql-extraction.txt"
defaultName="unielwin"
mysqlCommand="mysql -u root -B"

while getopts "n:p:" argument
do
  case $argument in
    n) name=$OPTARG
       ;;
    p) password=$OPTARG
  esac
done

if [[ -z $name ]]
then
  name=$defaultName
fi

if [[ -n $password ]]
then
  mysqlCommand="$mysqlCommand -p$password -e"
else
  mysqlCommand="$mysqlCommand -e"
fi

>$output

any_failure=0

for database in "${name}_BN" "${name}_CT" "${name}_setup"
do
  extractionFailed=0
  echo "Now extracting tables in database: $database..."

  tables=$($mysqlCommand "use $database; show tables;" | tail -n +2)

  if [[ $? -ne 0 ]]
  then
    extractionFailed=1
    any_failure=1
  fi

  for table in $tables
  do
    if [[ $table == "CallLogs" ]]
    then
      continue
    fi

    echo "  Extracting table: $table"
    echo "Table: $table" >> $output
    $mysqlCommand "use $database; select * from \`$table\`;" | sort -f >> $output
    if [[ $? -ne 0 ]]
    then
      extractionFailed=1
      any_failure=1
    fi
  done

  if [[ $extractionFailed -eq 0 ]]
  then
    echo -e "\n  ${GREEN}Extraction Complete!${CLEAR}\n"
  else
    echo -e "\n  ${RED}Extraction Failed!${CLEAR}\n"
  fi
done

if [[ $any_failure -eq 1 ]]
then
  echo -e "${RED}Failed to extract data!${CLEAR}"
  exit 1
else
    echo -e "  ${GREEN}ALL GOOD!${CLEAR}\n"
    echo -e "  The extracted data can be found in $output.\n"
fi