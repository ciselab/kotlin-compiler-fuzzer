#!/bin/bash

# First arg specifies the directory the files reside in
# Second arg specifies the filename (without the extension)
# Third and fourth args determine the output subdirectories for k1 and k2, respectively
# Fifth arg is the (shared) csv feature file
# Example:
# bash src/scripts/utils/extract_compiler_features.sh output 877fa2c6-9fa7-48c0-98b9-668d7cd74161 v1 v2 out.csv

wd=$(pwd)
compiler_command="$wd/src/main/resources/kotlinc/bin/kotlinc $wd/$1/$2.kt -Xrender-internal-diagnostic-names"

declare -a compiler_options=("-language-version 1.9 -d $wd/$1/$3/$2.jar" "-language-version 2.0 -d $wd/$1/$4/$2.jar")
declare -a output_objects=("$wd/$1/$3/$2.jar" "$wd/$1/$4/$2.jar")
declare -a log_files=("$wd/$1/$3/$2.txt" "$wd/$1/$4/$2.txt")

features=""
for val in 0 1; do
  cmmd="$compiler_command ${compiler_options[$val]}"
  /usr/bin/time -f "%e,%M" --quiet --output=out.txt --append $cmmd > ${log_files[$val]} 2>&1
  test -f ${output_objects[$val]}
  file_exists=$?

  if [ $file_exists -eq 0 ]
  then
    sz=$(ls -s -k ${output_objects[$val]} | awk '{print $1}')
  else
    sz=0
  fi

  features="$features$file_exists,$(cat out.txt),$sz,"
  rm out.txt 2> /dev/null
done

echo -n $features >> $wd/$5