from pydantic import BaseModel

class Code(BaseModel):
    name: str
    text: str