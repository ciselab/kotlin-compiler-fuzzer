from fastapi import FastAPI

from app.model import Code2Vec
from app.request_body import Code

app = FastAPI()
model = Code2Vec()

@app.get("/code2vec/")
def get_embedding(code: Code):
    return {"embedding":model.encode(code=code.text)}