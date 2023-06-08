FROM openjdk:21-ea-slim-bullseye

RUN apt update -y && apt install time -y

WORKDIR /kotfuzz

# RUN curl https://github.com/detekt/detekt/releases/download/v1.22.0/detekt-cli-1.22.0.zip

# RUN sudo apt-get install unzip

# RUN unzip detekt-cli-1.22.0.zip

# RUN alias detekt="/detekt/detekt-cli-1.22.0/bin/detekt-cli"

COPY detekt/ /kotfuzz/detekt/

COPY src/scripts/utils /kotfuzz/src/scripts/utils

COPY src/main/resources /kotfuzz/src/main/resources

ENTRYPOINT [ "/bin/bash" ]