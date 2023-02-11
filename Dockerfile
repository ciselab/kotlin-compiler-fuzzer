FROM gradle:7.6.0-jdk17-alpine

WORKDIR home/fuzzer

COPY build.gradle .
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle .
COPY local_dependencies ./local_dependencies
COPY src .

CMD ["gradle", "build"]