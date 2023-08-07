#!/bin/bash

# Build and the data packed volume container
docker build -t kotfuzz-dpvc Docker/dpvc/

# Build and the embedding service
docker build -t kotfuzz-embedding-service services/embedding_service -f Docker/embedding/Dockerfile

# Build the clustering service
docker build -t kotfuzz-clustering-service services/clustering_service -f Docker/clustering/Dockerfile

# Build the oom container service
docker build -t kotfuzz-oom-classifier-service services/oom_classifier_service/ -f Docker/oom/Dockerfile

# Build the compiler service 
docker build -t kotfuzz-compiler-service services/compiler_service/ -f Docker/compiler/Dockerfile

# Build the fuzzer service
docker build -t kotfuzz-fuzzer . -f Docker/fuzzer/Dockerfile

# Run the dpvc
# docker run -d -v /data/targets -v /data/targets-raw -v /data/targets-embedding-flat --name kotfuzz-dpvc kotfuzz-dpvc

# docker run -d --rm -p 9090:80 --name kotfuzz-embedding kotfuzz-embedding-service

# docker run -d --rm -p 9091:80 --name kotfuzz-clustering -v $(pwd)/services/clustering_service/default_models:/models  kotfuzz-clustering-service

# docker run -d --rm -p 9092:80 --name kotfuzz-oom -v $(pwd)/services/oom_classifier_service/default_models:/models -e MODELFILE=adaboost-oom -e MODELNAME=adaboost kotfuzz-oom-classifier-service

