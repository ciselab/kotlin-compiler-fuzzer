#!/bin/bash

# Postprocess directories named $1-[1..$2]
# Example:
# bash src/scripts/utils/postprocess_multiple.sh output 5

counter=1
while [ $counter -le $2 ]
do
  echo $(date)
  echo $counter
  bash src/scripts/utils/postprocess.sh $1-$counter stats.csv
  ((counter++))
done
