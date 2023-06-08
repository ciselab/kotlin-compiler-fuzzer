from fastapi import FastAPI
from os import getenv, listdir
from pickle import load


app = FastAPI()
algorithm_dict = dict()

MODEL_DIR = getenv("MODELDIR")
print(listdir(MODEL_DIR))
for f in listdir(MODEL_DIR):
    with open(f"{MODEL_DIR}/{f}", "rb") as model_f:
        centers = load(model_f)
        algorithm_dict[f] = centers

@app.post("/centers/{algorithm}/")
def get_clustering(algorithm: str):

    if algorithm not in algorithm_dict:
        return {"error" : f"Algorithm {algorithm} not found"}
    else:
        return algorithm_dict[algorithm].tolist()
    
@app.post("/algorithms/")
def algorithm_names():
    return [n for n in algorithm_dict]
