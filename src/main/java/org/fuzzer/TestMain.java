package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.fuzzer.dt.DTRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException, RecognitionException, ClassNotFoundException {
        String ctxFileName = "context.ser";

        String lexerGrammarFile = "resources/antlr/KotlinLexer.g4";
        String grammarFile = "resources/antlr/KotlinParser.g4";
        String classPath = "resources/kotlin/";
        String outputDir = "output/";
        String configPath = "configs/proximity/cfg-proximity-ga-50.yaml";

        List<String> classes = new ArrayList<>(List.of("Any.kt", "Comparable.kt", "Throwable.kt",
                "Array.kt", "Enum.kt", "Iterator.kt", "Library.kt", "Collections.kt", "Unit.kt", "ExceptionsH.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"));
        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();

        DTRunner runner = new DTRunner(
                fileNames, outputDir, configPath,
                0, 0, 0, 0, 0,
                ctxFileName, lexerGrammarFile, grammarFile, true);

        // 5 minutes
        runner.run(480000L, 10000L, true);

    }
}
