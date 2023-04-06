package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.fuzzer.dt.DTRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException, InterruptedException, ClassNotFoundException {
        String ctxFileName = "context.ser";


        String lexerGrammarFile = System.getProperty("lexerFile"); // "./src/main/resources/KotlinLexer.g4"
        String grammarFile = System.getProperty("grammarFile"); // "./src/main/resources/KotlinParser.g4"
        String compilerPath = System.getProperty("kotlinc"); // "src/main/resources/kotlinc/bin/kotlinc"
        String classPath = System.getProperty("classPath"); // "src/test/resources/kotlin/"
        String compilerScriptPath = System.getProperty("compilerScriptPath"); //"src/scripts/compile_file.sh"
        Long seed = Long.parseLong(System.getProperty("seed"));
        Long time = Long.parseLong(System.getProperty("time"));
        String output = System.getProperty("output");
        String configPath = System.getProperty("configFile"); // "./config.yaml"

        System.out.println(output);

        List<String> classes = new ArrayList<>(List.of("Any.kt", "Comparable.kt", "Throwable.kt",
                "Array.kt", "Enum.kt", "Iterator.kt", "Library.kt", "Collections.kt", "Unit.kt", "ExceptionsH.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"));
        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();

        List<String> compilerArgs = new ArrayList<>();
        compilerArgs.add("");
        compilerArgs.add("-Xuse-k2");

        DTRunner runner = new DTRunner(10000, 5,
                fileNames, output,
                compilerPath, compilerArgs,
                compilerScriptPath, configPath,
                0, 3, ctxFileName,
                lexerGrammarFile, grammarFile,
                false);
        runner.run(seed, time);
    }
}
