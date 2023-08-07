FROM gradle:8.0.0-jdk17-jammy AS build
WORKDIR /home/gradle/fuzzer

COPY src ./src
COPY settings.gradle ./settings.gradle
COPY build.gradle ./build.gradle
COPY local_dependencies ./local_dependencies

RUN gradle uberJar --no-daemon

FROM openjdk:17-slim-buster
#FROM ubuntu:latest

WORKDIR /fuzzer

COPY --from=build /home/gradle/fuzzer/build/libs/kotlin-compiler-fuzzer-0.1.0-uber.jar ./fuzzer.jar

ENTRYPOINT ["java"]