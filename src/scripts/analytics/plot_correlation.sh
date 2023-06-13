#!/bin/bash

# First arg contains the (csv) input data.

temp_file="temp_out.txt"

Rscript anova.r -i $1 -o $temp_file

csplit --digits=1 --quiet --prefix=outfile $temp_file "/---/+1" "{*}"

# Process the file model
file="outfile0"
echo "criterion,mean_sq" > filemodel.csv
cat $file | sed -n '/Residuals/q;p' | tail -n +2 | sort -k 3 -n  | awk '{ print $1","$3}' >> filemodel.csv

# Process the compiler model
file="outfile1"
echo "criterion,mean_sq" > compilermodel.csv
cat $file | sed -n '/Residuals/q;p' | tail -n +3 | sort -k 3 -n | awk '{ print $1","$3}' >> compilermodel.csv

# Process the AICC analysis
file="outfile2"
echo "model,aicc" > aicc.csv
cat $file | tail -n +6 | head -n +2 | awk '{ print $1","$3}' >> aicc.csv

Rscript anova_plot.r

rm $temp_file outfile0 outfile1 outfile2 aicc.csv compilermodel.csv filemodel.csv