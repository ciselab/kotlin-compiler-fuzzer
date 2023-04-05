#!/bin/bash

# First arg specifies the file to compile.
compiler_command="src/main/resources/kotlinc/bin/kotlinc $1 -Xrender-internal-diagnostic-names"
full_command=""

# Third arg specifies whether to use K2.
if [ $4 = "true" ]; then
  compiler_command="$compiler_command -language-version 2.0"
else
  compiler_command="$compiler_command -language-version 1.9"
fi

# Second arg specifies the output of the compiled objects
compiler_command="$compiler_command -d $2"

# Fifth arg specifies whether to profile the memory usage
if [ $5 = "true" ]; then
  # Third arg specifies the output of the logs
  /usr/bin/time -f "%K %M" "$compiler_command" 2>&1 | tail -n 1 | head -n 1 >> "$3"
else
  # Third arg specifies the output of the logs
  $compiler_command 2> $3
fi
