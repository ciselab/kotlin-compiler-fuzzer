FROM gradle:7.6.0-jdk17-alpine

WORKDIR home/fuzzer

COPY . .

CMD ["gradle", "build"]