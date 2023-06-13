from transformers import RobertaTokenizer, RobertaModel

from os import listdir
from tqdm import tqdm

class Code2Vec:
    def __init__(self, target_dir = None, model_name = "microsoft/codebert-base"):
        self.tokenizer = RobertaTokenizer.from_pretrained(model_name)
        self.model = RobertaModel.from_pretrained(model_name)
        self.targets = []
        if target_dir is not None:
            for t in tqdm(listdir(target_dir)):
                with open(f"{target_dir}/{t}") as f:
                    lines = ''.join(f.readlines())
                    self.targets.append(self.encode(lines))

    def encode(self, code, pooling_method='pooler_output', truncation=True):
        return self.model.forward(**self.tokenizer(
            code, padding=True, return_tensors='pt', truncation=truncation
        ))[pooling_method].detach().numpy().tolist()[0]