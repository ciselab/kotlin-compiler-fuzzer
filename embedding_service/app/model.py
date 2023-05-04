from transformers import RobertaTokenizer, RobertaModel

class Code2Vec:
    def __init__(self, model_name = "microsoft/codebert-base"):
        self.tokenizer = RobertaTokenizer.from_pretrained(model_name)
        self.model = RobertaModel.from_pretrained(model_name)

    def encode(self, code, pooling_method='pooler_output', truncation=True):
        return self.model.forward(**self.tokenizer(
            code, padding=True, return_tensors='pt', truncation=truncation
        ))[pooling_method].detach().numpy().tolist()[0]