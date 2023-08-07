import argparse
import pandas as pd

import os

from tqdm import tqdm

def contains_error_of_type(error_log_path, error_id):
    if not os.path.exists(error_log_path):
        print(f'File not found: {error_log_path}. Assuming analysis error occurred.')
        return True

    with open(f'{error_log_path}') as f:
        lines = ''.join(f.readlines())
        if error_id in lines:
            return True
        
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
                    prog='Size and Structure Data Preparation',
                    description='Parse metadata for files passed through the DT pipeline')
    
    parser.add_argument('-d','--dirs', nargs='+', dest='dirs', help='The list of absolute paths of directories containing generated kotlin files.', required=True)
    parser.add_argument('-n','--names', nargs='+', help='The names corresponding to the algorithms that were used to generate the files.', required=True)

    args = parser.parse_args()

    for c0, d in enumerate(tqdm(args.dirs)):
        names = []
        times = []
        error_types = []
        snapshots = []
        cummulative_bugs = []
        sizes = []

        snap_number = len(os.listdir(d))
        
        for c1, sn in enumerate(tqdm(os.listdir(d))):
            
            stats = pd.read_csv(f'{d}/{sn}/stats.csv')


            for c, f in enumerate(os.listdir(f'{d}/{sn}')):
                if not f.endswith(".kt"):
                    continue
                
                fname = f.split(".")[0]
                time = int(stats.loc[stats['file'] == fname].iloc[0]['time'])
                
                with open(f'{d}/{sn}/{f}') as fi:
                    size = len(''.join(fi.readlines()))

                k1_failed = not os.path.exists(f'{d}/{sn}/v1/{fname}.jar')
                k2_failed = not os.path.exists(f'{d}/{sn}/v2/{fname}.jar')
                
                failure_type = 0
                
                if k1_failed and k2_failed:
                    if contains_error_of_type(f'{d}/{sn}/v1/{fname}.txt', 'UNRESOLVED_REFERENCE'):
                        failure_type = -1
                    else:
                        failure_type = 5
                elif k1_failed and not k2_failed:
                    failure_type = 1
                elif (not k1_failed) and k2_failed:
                    failure_type = 3
                else:
                    failure_type = 0                

                names.append(fname)
                times.append(time)
                error_types.append(failure_type)
                sizes.append(size)
                snapshots.append(sn.split('-')[-1] if sn != 'final' else snap_number)


            # cummulative_bugs.append((cummulative_bugs[-1] if c > 0 else 0) + (1 if failure_type != 0 else 0))

        res_df = pd.DataFrame({'name':names,
                            'alg_name':[args.names[c0] for _ in range(len(names))], # this is silly
                            'time':times,
                            'error_type':error_types,
                            'snapshot': snapshots,
                            'size':sizes}).sort_values(by=['time'])
        
        sum_bugs = 0
        for _, row in res_df.iterrows():
            if row['error_type'] > 0:
                sum_bugs += 1
            cummulative_bugs.append(sum_bugs)

        res_df['cummulative'] = cummulative_bugs
        res_df.to_csv(f'{args.names[c0]}.csv')
    
