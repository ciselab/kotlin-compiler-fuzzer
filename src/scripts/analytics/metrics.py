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

def diversity_dfs(dfs, size_thresh = 500):
    means = [diversity_df(df, size_thresh) for df in dfs]

    return np.mean(means), np.std(means)

def diversity_df(df, size_thresh = 500):
    diversity_features = ['chars', 'attr', 'method', 'constr', 'simple_expr', 'do_while', 'assignment', 'try_catch', 'if_expr', 'elvis_op', 'simple_stmt']
    filtered_df = df[df.chars.le(size_thresh)]
    df_np = filtered_df.loc[:, diversity_features].to_numpy()

    return np.mean([np.sum([distance(p, p2) for p2 in df_np]) / (len(df_np) - 1) for p in df_np])

def distance(p1, p2):
    temp = p1 - p2
    return np.sqrt(np.dot(temp.T, temp))
