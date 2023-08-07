package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.fuzzer.dt.DTRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException, ClassNotFoundException {
        String ctxFileName = "context.ser";


        String lexerGrammarFile = System.getProperty("lexerFile"); // "resources/KotlinLexer.g4"
        String grammarFile = System.getProperty("grammarFile"); // "resources/KotlinParser.g4"
        String classPath = System.getProperty("classPath"); // "resources/kotlin/"
        int ctxSeed = Integer.parseInt(System.getProperty("ctxSeed"));
        int searchSeed = Integer.parseInt(System.getProperty("searchSeed"));
        int selectionSeed = Integer.parseInt(System.getProperty("selectionSeed"));
        int mutationSeed = Integer.parseInt(System.getProperty("mutationSeed"));
        int recombinationSeed = Integer.parseInt(System.getProperty("recombinationSeed"));
        Long time = Long.parseLong(System.getProperty("time"));
        boolean takeSnapshots = Boolean.parseBoolean(System.getProperty("takeSnapshots"));
        Long snapshotInterval = Long.parseLong(System.getProperty("snapshotInterval"));
        String output = System.getProperty("output");
        String configPath = System.getProperty("configFile"); // "config.yaml"

//        java -DlexerFile="/resources/antlr/KotlinLexer.g4" -DgrammarFile="/resources/antlr/KotlinParser.g4" -DcompilerPath="/resources/kotlinc" -DclassPath="/resources/kotlin/" -DctxSeed=0 -DsearchSeed=0 -DselectionSeed=0 -DmutationSeed=0 -DrecombinationSeed=0 -Dtime=120000 -DtakeSnapshots="false" -DsnapshotInterval=20000 -Doutput="/output" -DconfigFile="configs/rs/cfg-0.2.yaml" -jar fuzzer.jar
        List<String> classes = new ArrayList<>(List.of("Any.kt", "Comparable.kt", "Throwable.kt",
                "Array.kt", "Enum.kt", "Iterator.kt", "Library.kt", "Collections.kt", "Unit.kt", "ExceptionsH.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"));
        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();

        List<String> compilerArgs = new ArrayList<>();
        compilerArgs.add("");
        compilerArgs.add("-Xuse-k2");

        DTRunner runner = new DTRunner(fileNames, output,
                configPath, ctxSeed, searchSeed,
                selectionSeed, mutationSeed,
                recombinationSeed, ctxFileName,
                lexerGrammarFile, grammarFile,
                false);
        runner.run(time, snapshotInterval, takeSnapshots);
    }
}
