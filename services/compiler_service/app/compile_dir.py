from app.compiler_errors import CompilerError, CompilerVariant

from os import getenv, listdir, mkdir
from os.path import exists
from multiprocessing import Process
from time import sleep

import argparse
import subprocess

COMPILER_SCRIPT_PATH = getenv("COMPILERSCRIPT")
COMPILER_PATH = getenv("COMPILERPATH")
INPUT_DIR = getenv("INPUTDIR")
OUTPUT_DIR = getenv("OUTPUTDIR")

def read_kt_input_dir(input_dir=INPUT_DIR):
    return [f[:-3] for f in listdir(input_dir) if f.endswith(".kt")]

def read_kt_output_dir(output_dir=OUTPUT_DIR):
    return [f[:-4] for f in listdir(f"{output_dir}/v1") if f.endswith(".txt")]

def get_unprocessed_files(input_dir=INPUT_DIR, output_dir=OUTPUT_DIR):
    all_files = read_kt_input_dir(input_dir)
    processed_files = read_kt_output_dir(output_dir)

    return [f for f in all_files if f not in processed_files]

def process_input_file(file_uuid, input_dir=INPUT_DIR, output_dir=OUTPUT_DIR):
    cmd = ["bash", COMPILER_SCRIPT_PATH, input_dir, file_uuid, f"{output_dir}/v1", f"{output_dir}/v2", COMPILER_PATH]
    subprocess.run(cmd)

def process_output_file(file_uuid, output_dir=OUTPUT_DIR):
    error_k1 = None
    error_k2 = None

    k1_exists = exists(f"{output_dir}/v1/{file_uuid}.jar")
    k2_exists = exists(f"{output_dir}/v2/{file_uuid}.jar")

    error_type = None

    if not (k1_exists or k2_exists):
        error_type = CompilerVariant.BOTH
    elif not k1_exists:
        error_type = CompilerVariant.K1
    elif not k2_exists:
        error_type = CompilerVariant.K2
    else:
        error_type = CompilerVariant.NEITHER

    if not k1_exists:
        with open(f"{output_dir}/v1/{file_uuid}.txt") as fk1:
            text = "".join(fk1.readlines())
            error_k1 = process_log_crash_data(text)
    else:
        error_k1 = CompilerError.NONE

    if not k2_exists:
        with open(f"{output_dir}/v2/{file_uuid}.txt") as fk2:
            text = "".join(fk2.readlines())
            error_k2 = process_log_crash_data(text)
    else:
        error_k2 = CompilerError.NONE

    return error_type, error_k1, error_k2

def process_log_crash_data(text):
    if 'Backend Internal error: Exception during IR lowering' in text:
        return CompilerError.IR
    
    if 'exception: java.lang.OutOfMemoryError:' in text:
        return CompilerError.OOM
    
    if '[CONFLICTING_OVERLOADS]' in text:
        return CompilerError.CONFLICTING_OVERLOADS
    
    if '[OVERLOAD_RESOLUTION_AMBIGUITY]' in text:
        return CompilerError.RESOLUTION_AMBIGUITY
    
    return CompilerError.NONE

def process_files_dir(wait_for_new_files, input_dir, output_dir):
    if not exists(f"{output_dir}/v1"):
        mkdir(f"{output_dir}/v1")

    if not exists(f"{output_dir}/v2"):
        mkdir(f"{output_dir}/v2")

    first_iter = True

    while wait_for_new_files or first_iter:
        first_iter = False

        files_to_process = get_unprocessed_files(input_dir, output_dir)
        if not files_to_process:
            sleep(5)

        for f in files_to_process:
            process_input_file(f, input_dir, output_dir)

def get_unprocessed_directories(processed_directories, input_dir):
    return [d for d in listdir(input_dir) if d not in processed_directories]

def process_snapshots_dir(wait_for_new_snapshots, input_dir, num_parallel):
    processed_directories = []

    while wait_for_new_snapshots:
        # print(f'{processed_directories}; {input_dir}: {listdir(input_dir)}')
        dirs_to_process = get_unprocessed_directories(processed_directories, input_dir)
        if not dirs_to_process:
            print(f"No new snapshots detected.")
            sleep(5)

        proc_dirs = dirs_to_process[:min(num_parallel, len(dirs_to_process))]

        ps = [Process(target=process_files_dir,
                      args=(False, f'{input_dir}/{dirs_to_process[i]}', f'{input_dir}/{dirs_to_process[i]}'))
                      for i in range(min(num_parallel, len(dirs_to_process)))]
        for p in ps:
            p.start()
        
        for p in ps:
            p.join()

        processed_directories.extend(proc_dirs)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
                    prog='Kotfuzz compiler service',
                    description='Perform differential testing on the compiler')
    
    parser.add_argument('-m','--mode', choices=['files', 'snapshots'], dest='mode',
                        help='Whether the service should process snapshots or a pool of files.',
                        required=True)
    
    parser.add_argument('-n','--number-compilers', type=int, dest='ncomps',
                        help='The maximum number of compiler services to run at the same time.',
                        required=True)

    args = parser.parse_args()

    stop = False

    if args.mode == 'files':
        process_files_dir(True, INPUT_DIR, OUTPUT_DIR)
    else:
        process_snapshots_dir(True, INPUT_DIR, args.ncomps)
