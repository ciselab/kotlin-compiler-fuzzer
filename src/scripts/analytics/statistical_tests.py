import scipy.stats as stats

from data_handling import get_total_bugs
from metrics import auc_trapezoid_dfs

def wilcoxon(l1, l2):
    min_d = min(len(l1), len(l2))
    return stats.wilcoxon(l1[:min_d], l2[:min_d], alternative='two-sided')

def wilcoxon_effectiveness_dfs(df1s, df2s):    
    return wilcoxon(get_total_bugs(df1s), get_total_bugs(df2s))

def wilcoxon_efficiency_dfs(df1s, df2s):
    return wilcoxon(auc_trapezoid_dfs(df1s), auc_trapezoid_dfs(df2s))

def a12(l1, l2):
    m = len(l1)
    n = len(l2)
    
    if m != n:
        raise ValueError("Mismatched data in a12 test.")

    r = stats.rankdata(l1 + l2)
    r1 = sum(r[0:m])
    
    return (2 * r1 - m * (m + 1)) / (2 * n * m)

def a12_effectiveness_dfs(df1s, df2s):
    return a12(get_total_bugs(df1s), get_total_bugs(df2s))

def a12_efficiency_dfs(df1s, df2s):
    return a12(auc_trapezoid_dfs(df1s), auc_trapezoid_dfs(df2s))