from os import getenv
from transformers import RobertaTokenizer, RobertaModel

if __name__ == "__main__":
    MODELNAME = getenv("MODELNAME")
    RobertaTokenizer.from_pretrained(MODELNAME)
    RobertaModel.from_pretrained(MODELNAME)