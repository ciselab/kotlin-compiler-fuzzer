FROM gradle:8.0.0-jdk17-jammy AS build
WORKDIR /home/gradle/fuzzer
COPY . .
RUN gradle uberJar --no-daemon

FROM openjdk:17-slim-buster

WORKDIR /home/fuzzer

COPY --from=build /home/gradle/fuzzer/build/libs/kotlin-compiler-fuzzer-0.1.0-uber.jar /home/fuzzer/fuzzer.jar
COPY --from=build /home/gradle/fuzzer/src/main/resources /home/fuzzer/resources
COPY --from=build /home/gradle/fuzzer/src/test/resources/ /home/fuzzer/resources

RUN mkdir "output"

ENTRYPOINT ["java", "-DlexerFile=./resources/KotlinLexer.g4", "-DgrammarFile=./resources/KotlinParser.g4", "-Dkotlinc=./resources/kotlinc/bin/kotlinc", "-DclassPath=./resources/kotlin/", "-jar", "fuzzer.jar"]