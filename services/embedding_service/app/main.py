from typing import List, Union
from fastapi import FastAPI, Query
from os import getenv
from typing_extensions import Annotated
from pickle import load

from app.model import Code2Vec
from app.request_body import Code


app = FastAPI()

with open(getenv("MODELFILE"), "rb") as f:
    model = load(f)

@app.post("/embedding-multiple/")
def get_embedding(code_list: Annotated[Union[List[Code], None], Query()] = None):
    return {c.name : model.encode(code=c.text) for c in code_list}

@app.post("/embedding-single/")
def get_embedding(code: Code):
    return {"embedding":model.encode(code=code.text)}

@app.post("/targets/")
def get_targets():
    return model.targets