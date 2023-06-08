FROM alpine/git:2.36.3

WORKDIR /data

RUN git clone --depth 1 --branch v1.8.21 https://github.com/JetBrains/kotlin.git

RUN mkdir targets-raw targets targets-embedding-flat

RUN cp -R kotlin/compiler/testData/* targets-raw/

RUN find kotlin/compiler/testData -type f -name "*.kt" > temp_files.txt

RUN while IFS= read -r fname; do mv "$fname" targets; done < temp_files.txt

RUN rm temp_files.txt && rm -rf kotlin

ENTRYPOINT [ "/bin/sh" ]