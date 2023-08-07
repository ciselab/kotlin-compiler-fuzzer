#!/bin/bash

# Example: bash src/scripts/utils/compile_dir_simple.sh output src/main/resources/kotlinc/bin/kotlinc
files=$(ls "$1"/*.kt*)

for file in $files;
do
  base=$(basename "$file")
  bash src/scripts/utils/compile_file_simple.sh "$1" "${base%.*}" "$(pwd)/$1/v1" "$(pwd)/$1/v2" "$2"
done