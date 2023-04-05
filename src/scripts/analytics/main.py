import argparse
import os
import sys

from data_handling import append_cummulative_bugs, get_data_from_dir, get_dirs, get_total_bugs
from statistical_tests import a12_effectiveness_dfs, a12_efficiency_dfs, auc_trapezoid_dfs, wilcoxon_effectiveness_dfs, wilcoxon_efficiency_dfs

import numpy as np

def analyze_results(directories_alg1, directories_alg2, p_thresh = 0.05):
    """
    Assumes post-processed directories.
    """
    
    df1s = [append_cummulative_bugs(get_data_from_dir(output_dir)) for output_dir in directories_alg1]
    df2s = [append_cummulative_bugs(get_data_from_dir(output_dir)) for output_dir in directories_alg2]
    
    print("=== Effectiveness ===")
    print(f"Algorithm 1 causes on average {np.mean(get_total_bugs(df1s))} crashes")
    print(f"Algorithm 2 causes on average {np.mean(get_total_bugs(df2s))} crashes")
    
    _, wc_p = wilcoxon_effectiveness_dfs(df1s, df2s)
    print(f"p-value of two-sided Wilcoxon test: {wc_p} is {'' if wc_p < p_thresh else 'not '}significant")
    if wc_p < p_thresh:
        a12_effectiveness_d1 = a12_effectiveness_dfs(df1s, df2s)
        a12_effectiveness_d2 = a12_effectiveness_dfs(df2s, df1s)
        
        if (a12_effectiveness_d1 > a12_effectiveness_d2):
            print(f"A12 statistic in favor of algorithm 1: {a12_effectiveness_d1}")
        else:
            print(f"A12 statistic in favor of algorithm 2: {a12_effectiveness_d2}")

            
    print("=== Efficiency ===")
    
    auc_d1 = auc_trapezoid_dfs(df1s)
    auc_d2 = auc_trapezoid_dfs(df2s)
    
    print(f"Algorithm 1 mean AUC: {np.mean(auc_d1)}")
    print(f"Algorithm 2 mean AUC: {np.mean(auc_d2)}")
#     print(f"Mean AUC: algorithm1={np.mean(auc_d1)},std={np.std(auc_d1)}, algorithm2={np.mean(auc_d2)},std={np.std(auc_d2)}")
    
    _, wc_p_auc = wilcoxon_efficiency_dfs(df1s, df2s)
    print(f"p-value of two-sided Wilcoxon test: {wc_p_auc} is {'' if wc_p_auc < p_thresh else 'not'} significant")
    if wc_p_auc < p_thresh:
        a12_efficiency_d1 = a12_efficiency_dfs(df1s, df2s)
        a12_efficiency_d2 = a12_efficiency_dfs(df2s, df1s)
        
        if (a12_efficiency_d1 > a12_efficiency_d2):
            print(f"A12 statistic in favor of algorithm 1: {a12_efficiency_d1}")
        else:
            print(f"A12 statistic in favor of algorithm 2: {a12_efficiency_d2}")

        

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
                    prog='Fuzzer Analytics',
                    description='Analyze fuzzer output directories')
    
    parser.add_argument('-d','--dirs', nargs='+', help='The list of directories to analyze. Full paths are expected.', required=True)
    parser.add_argument('-n','--names', nargs='+', help='The names corresponding to the algorithms that were used to generate the paths.', required=True)
    
    args = parser.parse_args()

    dirs = args.dirs
    names = args.names

    if (len(dirs) != len(dirs)):
        print("The number of names and dirs provided do not match.")
        sys.exit(1)

    for c, (d1, n1) in enumerate(zip(dirs, names)):
        for (d2, n2) in zip(dirs[c + 1:], names[c + 1:]):
            print(f'==== Comparing {n1} and {n2} ====')
            print(f'==== In directories {d1} and {d2} ====')

            analyze_results(get_dirs(d1), get_dirs(d2))
    