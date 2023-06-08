from app.compiler_errors import CompilerError, CompilerVariant

from os import getenv, listdir, mkdir
from os.path import exists
from time import sleep

import subprocess

COMPILER_SCRIPT_PATH = getenv("COMPILERSCRIPT")
COMPILER_PATH = getenv("COMPILERPATH")
INPUT_DIR = getenv("INPUTDIR")
OUTPUT_DIR = getenv("OUTPUTDIR")

def read_kt_input_dir():
    return [f[:-3] for f in listdir(INPUT_DIR) if f.endswith(".kt")]

def read_kt_output_dir():
    return [f[:-4] for f in listdir(f"{OUTPUT_DIR}/v1") if f.endswith(".txt")]

def get_unprocessed_files():
    all_files = read_kt_input_dir()
    processed_files = read_kt_output_dir()

    return [f for f in all_files if f not in processed_files]

def process_input_file(file_uuid):
    cmd = ["bash", COMPILER_SCRIPT_PATH, INPUT_DIR, file_uuid, f"{OUTPUT_DIR}/v1", f"{OUTPUT_DIR}/v2", COMPILER_PATH]
    subprocess.run(cmd)

def process_output_file(file_uuid):
    error_k1 = None
    error_k2 = None

    k1_exists = exists(f"{OUTPUT_DIR}/v1/{file_uuid}.jar")
    k2_exists = exists(f"{OUTPUT_DIR}/v2/{file_uuid}.jar")

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
        with open(f"{OUTPUT_DIR}/v1/{file_uuid}.txt") as fk1:
            text = "".join(fk1.readlines())
            error_k1 = process_log_crash_data(text)
    else:
        error_k1 = CompilerError.NONE

    if not k2_exists:
        with open(f"{OUTPUT_DIR}/v2/{file_uuid}.txt") as fk2:
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

if __name__ == "__main__":
    stop = False

    if not exists(f"{OUTPUT_DIR}/v1"):
        mkdir(f"{OUTPUT_DIR}/v1")

    if not exists(f"{OUTPUT_DIR}/v2"):
        mkdir(f"{OUTPUT_DIR}/v2")

    while not stop:
        files_to_process = get_unprocessed_files()
        if not files_to_process:
            print(f"No files to process at the minute")
            sleep(5)
        for f in files_to_process:
            print(f"processing {f}")
            process_input_file(f)
            print(f"result: {process_output_file(f)}")

