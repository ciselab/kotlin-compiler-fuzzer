from json import dump as jsondump
from os import getenv
from pickle import dump, HIGHEST_PROTOCOL

from app.model import Code2Vec

def setup(target_dir, model_output_file,
          embeddings_output_file, model_name = "microsoft/codebert-base"):
    model = Code2Vec(target_dir, model_name)
    
    with open(model_output_file, "wb") as out:
        dump(model, out, HIGHEST_PROTOCOL)

    with open(embeddings_output_file, "w+") as f:
        jsondump(model.targets, f, indent = 4)

if __name__ == "__main__":

    TARGET_DIR = getenv("TARGETDIR")
    MODEL_FILE = getenv("MODELFILE")
    EMBEDDING_FILE = getenv("EMBEDDINGFILE")
    MODEL_NAME = getenv("MODELNAME")
    
    setup(TARGET_DIR, MODEL_FILE, EMBEDDING_FILE, MODEL_NAME)