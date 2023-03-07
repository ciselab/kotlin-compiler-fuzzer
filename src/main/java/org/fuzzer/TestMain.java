package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.fuzzer.dt.DTRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException, RecognitionException, InterruptedException, ClassNotFoundException {
        String ctxFileName = "context.ser";

        String lexerGrammarFile = "./src/main/resources/KotlinLexer.g4";
        String grammarFile = "./src/main/resources/KotlinParser.g4";
        String compilerPath = "src/main/resources/kotlinc/bin/kotlinc";
        String classPath = "src/test/resources/kotlin/";

        List<String> classes = new ArrayList<>(List.of("Any.kt", "Comparable.kt", "Throwable.kt",
                "Array.kt", "Enum.kt", "Iterator.kt", "Library.kt", "Collections.kt", "Unit.kt", "ExceptionsH.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"));
        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();

        List<String> compilerArgs = new ArrayList<>();
        compilerArgs.add("");
        compilerArgs.add("-Xuse-k2");

        DTRunner runner = new DTRunner(1, 100,
                fileNames, "output/",
                compilerPath, compilerArgs,
                0, 3, ctxFileName,
                lexerGrammarFile, grammarFile);
        runner.run();
    }
}
