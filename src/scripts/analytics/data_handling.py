import os

import numpy as np
import pandas as pd

def get_data_from_dir(output_dir, stats_file="stats.csv"):
    return pd.read_csv(f"{output_dir}/{stats_file}")

def append_cummulative_bugs(df):
    cummulative_bugs = []
    s = 0
    for _, row in df.iterrows():
        s += 1 if ((row['k1_exit'] != 0 or row['k2_exit'] != 0) and (row['k1_exit'] != row['k2_exit'])) else 0
        cummulative_bugs.append(s)
    df['total_bugs'] = cummulative_bugs
    return df

def get_total_bugs(dfs):
    return np.array([df.iloc[-1].total_bugs for df in dfs])

def get_dirs(parent_dir):
    return [f'{parent_dir}/{d}' for d in os.listdir(parent_dir)]