import numpy as np
import pandas as pd

def auc_trapezoid(df):
    s = 0
    for index, row in df.iterrows():
        if index == 0:
            continue
        prev_row = df.iloc[index - 1]
        s += (row.total_bugs + prev_row.total_bugs) * (row.time - prev_row.time)
    s = (s * 0.5) / df.iloc[-1].time
    return s

def auc_trapezoid_dfs(dfs):
    return np.array([auc_trapezoid(df) for df in dfs])
