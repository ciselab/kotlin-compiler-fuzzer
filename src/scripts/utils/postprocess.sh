#!/bin/bash

# First arg is the directory containing the files
# Second arg is the file to append to (assumed to be in the output directory too)
# Example:
# bash src/scripts/postprocess.sh output stats.csv

cat $1/$2 | tail -n +2 | awk -F ',' '{print $1}' | while read line; do bash src/scripts/extract_compiler_features.sh $1 ${line} v1 v2 temp0.csv; bash src/scripts/extract_file_features.sh $1 ${line} temp0.csv; echo "" >> temp0.csv; done
cat $1/$2 | tail -n +2 > temp1.csv
exec {fdA}<temp1.csv
exec {fdB}<temp0.csv

cat $1/$2 | head -n 1 > temp2.csv
while read -r -u "$fdA" lineA && read -r -u "$fdB" lineB
do
    echo "$lineA $lineB" >> temp2.csv
done

rm temp0.csv
rm temp1.csv
mv temp2.csv $1/$2
