package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.fuzzer.dt.DTRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException, RecognitionException, InterruptedException, ClassNotFoundException {
        String ctxFileName = "context.ser";
        String compilerScriptPath = "src/scripts/compile_file.sh";

        String lexerGrammarFile = "resources/antlr/KotlinLexer.g4";
        String grammarFile = "resources/antlr/KotlinParser.g4";
        String compilerPath = "resources/kotlinc/bin/kotlinc";
        String classPath = "resources/kotlin/";
        String outputDir = "output/";
        String configPath = "config.yaml";

        List<String> classes = new ArrayList<>(List.of("Any.kt", "Comparable.kt", "Throwable.kt",
                "Array.kt", "Enum.kt", "Iterator.kt", "Library.kt", "Collections.kt", "Unit.kt", "ExceptionsH.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"));
        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();

        List<String> compilerArgs = new ArrayList<>();
        compilerArgs.add("false");
        compilerArgs.add("true");

        DTRunner runner = new DTRunner(10000, 5,
                fileNames, outputDir,
                compilerArgs, configPath,
                0, 0, 0, 0, 0,
                3, ctxFileName,
                lexerGrammarFile, grammarFile,
                true);

        // 5 minutes
        runner.run(120000L, 20000L, true);

    }
}
