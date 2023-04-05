#!/bin/bash

# First arg: number of runs
# Second arg: number of processes to run at the same time

counter=1
while [ $counter -le $1 ]
do

  proc_number=1
  all_procs=()

  while [ $proc_number -le $2 ]
  do
      echo $counter
      mkdir -p output-$counter/v1 output-$counter/v2

      java -DlexerFile="./src/main/resources/KotlinLexer.g4" -DgrammarFile="./src/main/resources/KotlinParser.g4" -Dkotlinc="src/main/resources/kotlinc/bin/kotlinc" -DclassPath="src/test/resources/kotlin/" -Dtime="300000" -Dseed="$counter" -Doutput="output-$counter/" -jar ./build/libs/kotlin-compiler-fuzzer-0.1.0-uber.jar &

      all_procs[${proc_number}]=$!

      ((counter++))
      ((proc_number++))
  done;

  # Wait for all processes to finish
  for pid in ${all_procs[*]}; do
    echo "Waiting for $pid"
    wait $pid
  done

done
