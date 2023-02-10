package org.fuzzer;

import org.antlr.runtime.RecognitionException;
import org.fuzzer.dt.DTRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, RecognitionException, CloneNotSupportedException, InterruptedException {
        String classPath = "src/test/resources/kotlin/";
        List<String> classes = new ArrayList<>(List.of(new String[]{"Any.kt", "Comparable.kt",
                "Char.kt", "CharSequence.kt", "Number.kt", "Primitives.kt", "Boolean.kt", "String.kt"}));
        List<String> fileNames = classes.stream().map(x -> classPath + x).toList();

        List<String> compilerArgs = new ArrayList<>();
        compilerArgs.add("-d src/main/java/output/v1 > out1.txt");
        compilerArgs.add("-d src/main/java/output/v2 -Xuse-k2 > out2.txt");

        DTRunner runner = new DTRunner(1, 10,
                fileNames, "src/main/java/output/",
                "src/main/resources/kotlinc/bin/kotlinc", compilerArgs,
                0, 3);
        runner.run();
    }
}
