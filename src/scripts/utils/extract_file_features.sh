#!/bin/bash

# First arg is the directory the files reside in (for consistency with other scripts)
# Second arg is the name of the file (with no extension)
# Third arg is the csv output
# Example:
# bash src/scripts/extract_file_features.sh output 877fa2c6-9fa7-48c0-98b9-668d7cd74161 out.csv

detekt="$(pwd)/detekt/detekt-cli-1.22.0/bin/detekt-cli"
features=$($detekt --parallel --report md:rep.md --language-version 1.9 --config detekt/config.yml --input $1/$2.kt | grep "-" | awk '{print $2}' | sed 's/%//g' | sed 's/,//g' | tr '\n' ',' | sed 's/,$/\n/')
echo -n $features >> $(pwd)/$3
rm rep.md 2> /dev/null

# Output in csv format:
# loc sloc lloc cloc mcc congnitive smells cmmtratio mccperKLOC smellsperKLOC