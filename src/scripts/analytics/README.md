# Analytics scripts

This directory contains scripts that anlyze the post-processed output of the fuzzer.

## Effectiveness and efficiency

Effectiveness is measured in terms of total number of crashes. The area under curve metric measures the effectiveness of a run. To get the effectiveness and efficiency analytics, run the `main.py` script as follows:


```bash
python main.py -d alg1_dir ... algn_dir -n alg1_name ... algn_name
```

Each of the `algx_dir`s is assumed to have the following structure:


```
output-alg1  
│
└───output-1
│   │   file1.kt
│   │   ...
│   │   filen.kt
│   │   stats.csv
│   
└───output-2
│   │   file1.kt
│   │   ...
│   │   filen.kt
│   │   stats.csv
│   ...
```

The analytics scripts use the Wilcoxon signed rank and reports the p-values of each comparison. The scripts consider all possible paired comparisons.

## ANOVA test

The ANOVA test is used to analyze the correlation between the features of the generated files, the behavior of the compiler, and the number of crashes. The `plot_correlation.sh` script will generate correlation plots for both the compiler and file models, given an aggregated data set. The script can be run as follows:

```bash
bash plot_correlation.sh data.csv
```