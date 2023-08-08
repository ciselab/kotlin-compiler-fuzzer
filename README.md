# kotlin-compiler-fuzzer
Fuzzing and differential testing for the Kotlin compiler.

### Build and run locally

1. Run the `Docker/setup.sh` script
    - In addition to building the images, we provide several default options for ML models and other heuristic parameters. To retrieve those default values, simply run `dvc pull`. 
2. The repo contains a principal fuzzer component, as well as several additional modules in the `services` directory.
   - The `clustering` (or targets) service provides pre-defined targets used for the proximity heuristics. To start up this service, run `docker run -it --rm -p 9091:80 --name kotfuzz-clustering -v $(pwd)/services/clustering_service/default_models:/models  kotfuzz-clustering-service`.
   - The `embedding` service provides an API that transforms generated code to a vectorized representation through the use of code models. To run the embedding service, use `docker run -it --rm -p 9090:80 --name kotfuzz-embedding kotfuzz-embedding-service`.
   - The `compiler` service performs differential testing and can be run in parallel to, or independent of, the fuzzer.
   - The `oom` service API allows the fuzzer to dynamically check whether generated files are too large to result in interesting bugs. Since setting an appropriate simplicity bias in the configuration also solves this problem, we recommend against using this service, as to avoid overhead. To start the service, run its container: `docker run -d --rm -p 9092:80 --name kotfuzz-oom -v $(pwd)/services/oom_classifier_service/default_models:/models -e MODELFILE=adaboost-oom -e MODELNAME=adaboost kotfuzz-oom-classifier-service`.
3. Configure the fuzzer by following the `src/scripts/analytics/README.md` documentation.
4. Run the main fuzzing application by adapting the following command

```bash
docker run --name randfuzzer-test -it --rm\
 -v $(pwd)/output-pro-moga-50:/output\
 -v $(pwd)/resources:/resources:ro\
 -v $(pwd)/configs:/configs:ro\
 kotfuzz-fuzzer\
 -DlexerFile="/resources/antlr/KotlinLexer.g4"\
 -DgrammarFile="/resources/antlr/KotlinParser.g4"\
 -DcompilerPath="/resources/kotlinc"\
 -DclassPath="/resources/kotlin/"\
 -DctxSeed=168917\
 -DsearchSeed=168918\
 -DselectionSeed=168919\
 -DmutationSeed=168920\
 -DrecombinationSeed=168921\
 -Dtime=5400000 -DsnapshotInterval=180000\
 -DtakeSnapshots="true" -Doutput="/output"\
 -DconfigFile="/configs/proximity/moga/cfg-proximity-moga-50.yaml"\
 -jar fuzzer.jar
```

5. Either at the same time, or after the fuzzer run has completed, run the compiler service to process the generated files. Additional documentation resides in the `services/compiler/README.md` file, but the standard command below could be adapted.

```bash
docker run -it\
 -v $(pwd)/output-performance/diversity/diversity-5:/output -v $(pwd)/output-performance/diversity/diversity-5:/input\
 kotfuzz-compiler-service\
 -m snapshots -n 1
```

6. Gather and aggregate data through the analytics scripts. Use ither `python3 src/scripts/analytics/prepare_size_data_snapshots.py -d output-dir-0/ output-dir-1/ -n name0 name1` or `python3 src/scripts/analytics/prepare_size_data.py <...>` depending on whether the algorithm used to generate the files is a GA- or an RS-variant.
The resulting `name[x].csv` files contain the aggregated data.
