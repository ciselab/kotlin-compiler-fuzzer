FROM kotfuzz-dpvc as dpvc

# Rust for tokenizers...
FROM rust:buster

WORKDIR /src

COPY ./requirements.txt requirements.txt

RUN apt-get update

RUN apt-get install -y python3 python3-pip

# Update pip
RUN pip3 install --upgrade pip

# Install requirements
RUN pip3 install --no-cache-dir --upgrade -r requirements.txt

COPY ./app/setup_transformers.py /src/app/setup_transformers.py

ENV MODELFILE="/src/app/model.pkl"

ENV MODELNAME="microsoft/codebert-base"

RUN touch "$MODELFILE"

RUN python3 -m app.setup_transformers

COPY --from=dpvc /data/targets /data/targets

COPY --from=dpvc /data/targets-embedding-flat /data/targets-embedding-flat

ENV TARGETDIR="/data/targets"

ENV EMBEDDINGFILE="/data/targets-embedding-flat/embeddings.json"

COPY ./app/setup_model.py /src/app/setup_model.py

COPY ./app/model.py /src/app/model.py

RUN python3 -m app.setup_model

COPY ./app /src/app

EXPOSE 80

# Run the API
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"]