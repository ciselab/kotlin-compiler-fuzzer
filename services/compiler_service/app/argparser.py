from fastapi import FastAPI
from os import getenv, listdir

app = FastAPI()
algorithm_dict = dict()

INPUT_DIR = getenv("INPUTDIR")
OUTPUT_DIR = getenv("OUTPUT_DIR")

