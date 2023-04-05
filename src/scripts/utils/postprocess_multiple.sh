#!/bin/bash

# Postprocess directories named $1-[1..$2]
# Example:
# bash src/scripts/postprocess_multiple.sh output 5

counter=1
while [ $counter -le $2 ]
do
  echo $counter
  bash src/scripts/postprocess.sh $1-$counter stats.csv
  ((counter++))
done
