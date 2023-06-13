from app.model import *

from os import getenv
from json import load
from tqdm import tqdm
from pickle import dump, HIGHEST_PROTOCOL

def setup(data_file, output_dir):
    with open(DATA_FILE, "r") as f:
        data = np.array(load(f))

    algs = [KMeansCls(1928)] + [AgglomerativeCls(linkage, f"aggl_{linkage}") for linkage in ["ward", "single", "complete", "average"]]

    for c, alg in enumerate(tqdm(algs)):
        print(alg)
        alg.fit(data)
        print(alg.centers)

        with open(f"{output_dir}/{c}", "wb") as out:
            dump(alg, out, HIGHEST_PROTOCOL)


if __name__ == "__main__":
    DATA_FILE = getenv("DATAFILE")
    MODEL_DIR = getenv("MODELDIR")

    setup(DATA_FILE, MODEL_DIR)