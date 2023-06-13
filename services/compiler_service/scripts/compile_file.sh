#!/bin/bash

# First arg specifies the directory the files reside in
# Second arg specifies the filename (without the extension)
# Third and fourth args determine the output subdirectories for k1 and k2, respectively
# Fifth arg is the compiler path
# Example:
# bash src/scripts/utils/compile_file.sh input 877fa2c6-9fa7-48c0-98b9-668d7cd74161 /output/v1 /output/v2 /src/main/resources/kotlinc/bin/kotlinc

compiler_command="$5 $1/$2.kt -Xrender-internal-diagnostic-names"

declare -a compiler_options=("-language-version 1.9 -d $3/$2.jar" "-language-version 2.0 -d $4/$2.jar")
declare -a output_objects=("$3/$2.jar" "$4/$2.jar")
declare -a log_files=("$3/$2.txt" "$4/$2.txt")

features=""
for val in 0 1; do
  cmmd="$compiler_command ${compiler_options[$val]}"
  $cmmd > ${log_files[$val]} 2>&1
done