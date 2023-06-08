FROM openjdk:17-bullseye

WORKDIR /compiler

RUN wget -P /compiler https://github.com/JetBrains/kotlin/releases/download/v1.8.21/kotlin-compiler-1.8.21.zip

RUN unzip /compiler/kotlin-compiler-1.8.21.zip

RUN apt update && apt upgrade -y

RUN apt install -y python3 python3-pip nano

COPY requirements.txt /compiler/requirements.txt

RUN pip3 install -r requirements.txt

COPY ./app compiler/app

COPY ./scripts compiler/scripts

COPY ./app /compiler/app

COPY ./scripts /compiler/scripts

ENV COMPILERSCRIPT="/compiler/scripts/compile_file.sh"

ENV COMPILERPATH="/compiler/kotlinc/bin/kotlinc"

ENV INPUTDIR="/input"

ENV OUTPUTDIR="/output"

ENTRYPOINT [ "python3", "-m", "app.compile_dir" ]

