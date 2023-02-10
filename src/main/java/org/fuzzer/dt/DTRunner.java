package org.fuzzer.dt;


import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.generator.CodeGenerator;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.RandomNumberGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.fuzzer.utils.FileUtilities.compareByByte;

public class DTRunner {

    private final int numberOfFiles;
    private final int numberOfStatements;

    private final String directoryOutput;

    private final LexerGrammar lexerGrammar;

    private final Grammar parserGrammar;

    // Unused for now
    private final RuleHandler ruleHandler;

    private final Context rootContext;

    private final RandomNumberGenerator rng;

    private final int maxDepth;

    private final String kotlinCompilerPath;

    private final List<String> args;

    public DTRunner(int numberOfFiles, int numberOfStatements,
                    List<String> inputFileNames, String directoryOutput,
                    String kotlinCompilerPath, List<String> commandLineArgs,
                    int seed, int maxDepth) throws IOException, RecognitionException {
        this.numberOfFiles = numberOfFiles;
        this.numberOfStatements = numberOfStatements;
        this.directoryOutput = directoryOutput;
        this.kotlinCompilerPath = kotlinCompilerPath;
        this.args = commandLineArgs;
        this.maxDepth = maxDepth;

        File lexerFile = new File("./src/main/resources/KotlinLexer.g4");
        File parserFile = new File("./src/main/resources/KotlinParser.g4");

        lexerGrammar = new LexerGrammar(FileUtilities.fileContentToString(lexerFile));
        parserGrammar = new Grammar(FileUtilities.fileContentToString(parserFile));

        ruleHandler = new RuleHandler(lexerGrammar, parserGrammar);
        rng = new RandomNumberGenerator(seed);

        rootContext = new Context(rng);
        rootContext.fromFileNames(inputFileNames);

        // Add some dummy values to the context
        rootContext.addDefaultValue(rootContext.getTypeByName("Byte"), "(0x48 as Byte)");
        rootContext.addDefaultValue(rootContext.getTypeByName("Float"), "1.0f");
        rootContext.addDefaultValue(rootContext.getTypeByName("Double"), "2.0");
        rootContext.addDefaultValue(rootContext.getTypeByName("Int"), "3");
        rootContext.addDefaultValue(rootContext.getTypeByName("Short"), "(4.toShort())");
        rootContext.addDefaultValue(rootContext.getTypeByName("Long"), "(5.toLong())");
        rootContext.addDefaultValue(rootContext.getTypeByName("Boolean"), "true");
        rootContext.addDefaultValue(rootContext.getTypeByName("Boolean"), "false");
        rootContext.addDefaultValue(rootContext.getTypeByName("String"), "\"fooBar\"");
        rootContext.addDefaultValue(rootContext.getTypeByName("Char"), "'w'");
    }

    private List<Context> createContexts() {
        List<Context> ctxs = new ArrayList<>();

        for (int i = 0; i < numberOfFiles; i++) {
            ctxs.add(rootContext.clone());
        }

        return ctxs;
    }

    public void run() throws IOException, RecognitionException, CloneNotSupportedException, InterruptedException {
        List<Context> contexts = createContexts();

        for (int i = 0; i < numberOfFiles; i++) {
            CodeFragment code = new CodeFragment();
            CodeGenerator generator = new CodeGenerator(ruleHandler, rng, maxDepth, contexts.get(i));
            for (int j = 0; j < numberOfStatements; j++) {
                CodeFragment newCode = generator.sampleAssignment();
                code.extend(newCode);
            }

            String randomFilename = directoryOutput + UUID.randomUUID();

            String text = "fun main(args: Array<String>) {\n";
            text += code.getText();
            text += "\n}";

            String kotlinFile = randomFilename + ".kt";

            BufferedWriter writer = new BufferedWriter(new FileWriter(kotlinFile));
            writer.write(text);

            writer.close();

            List<Process> procs = new ArrayList<>();

            for (String compilerArgs : args) {
                System.out.println(kotlinCompilerPath + " " + kotlinFile + " " + compilerArgs);
                ProcessBuilder pb = new ProcessBuilder(kotlinCompilerPath, kotlinFile, compilerArgs);
                pb.directory(new File("/home/user/Master/thesis/kotlin-compiler-fuzzer/"));
                Process p = pb.start();
                procs.add(p);
            }

            for (Process p : procs) {
                p.waitFor();
                System.out.println(p.toString());
            }

            File compiled1 = new File(directoryOutput + "v1/" + kotlinFile);
            File compiled2 = new File(directoryOutput + "v2/" + kotlinFile);
            if (compareByByte(compiled1, compiled2) != -1) {
                System.out.println("Discrepancy detected!");
            }
        }
    }
}
