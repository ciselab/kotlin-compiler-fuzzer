from typing import List
from fastapi import FastAPI
from os import getenv
from pickle import load

from app.model import Classifier

import numpy as np

app = FastAPI()
model: Classifier = None

with open(f"{getenv('MODELDIR')}/{getenv('MODELFILE')}", "rb") as f:
    model = Classifier(getenv('MODELNAME'), load(f))

@app.post("/classify-multiple/")
def classify_multiple(data: List[List[float]]):
    return model(np.array(data)).tolist()

@app.post("/classify-single/")
def classify_single(data: List[float]):
    return model(np.array(data).reshape(1, -1)).tolist()

@app.post("/info/")
def get_info():
    return {"model":model.name}