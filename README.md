# kotlin-compiler-fuzzer
Differential fuzzer for testing different versions of the Kotlin compiler

### Build and run locally

1. Execute the `uberJar` gradle task (`gradle uberJar`)
2. Run the resulting `jar` file providing the required paths. For the current version, the command is:

```bash
java -DlexerFile="./src/main/resources/KotlinLexer.g4" -DgrammarFile="./src/main/resources/KotlinParser.g4" -Dkotlinc="src/main/resources/kotlinc/bin/kotlinc" -DclassPath="src/test/resources/kotlin/" -Dtime="300000" -jar ./build/libs/kotlin-compiler-fuzzer-0.1.0-uber.jar
```

3. Inspect the `output` directory for the generated files, compiled `jar`s, and compiler statistics.

### Running in a container

1. Build the image in the root directory (i.e., `docker build -t fuzzer .`)
2. Run the container and mount its output directory (i.e., `docker run -v $(pwd)/output:/home/fuzzer/output -it fuzzer:latest`)
