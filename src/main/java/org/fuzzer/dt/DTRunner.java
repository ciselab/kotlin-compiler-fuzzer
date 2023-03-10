package org.fuzzer.dt;


import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.GrammarTransformer;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.RandomNumberGenerator;
import org.jetbrains.kotlin.spec.grammar.tools.KotlinGrammarToolsKt;
import org.jetbrains.kotlin.spec.grammar.tools.KotlinParseTree;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.fuzzer.utils.FileUtilities.compareByByte;
import static org.jetbrains.kotlin.spec.grammar.tools.KotlinGrammarToolsKt.parseKotlinCode;
import static org.jetbrains.kotlin.spec.grammar.tools.KotlinGrammarToolsKt.tokenizeKotlinCode;

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
                    int seed, int maxDepth, String contextFileName,
                    String lexerFileName, String grammarFileName) throws IOException, RecognitionException, ClassNotFoundException {
        this.numberOfFiles = numberOfFiles;
        this.numberOfStatements = numberOfStatements;
        this.directoryOutput = directoryOutput;
        this.kotlinCompilerPath = kotlinCompilerPath;
        this.args = commandLineArgs;
        this.maxDepth = maxDepth;

        File lexerFile = new File(lexerFileName);
        File parserFile = new File(grammarFileName);

        lexerGrammar = new LexerGrammar(FileUtilities.fileContentToString(lexerFile));
        parserGrammar = new Grammar(FileUtilities.fileContentToString(parserFile));

        ruleHandler = new RuleHandler(lexerGrammar, parserGrammar);
        rng = new RandomNumberGenerator(seed);

        File ctxFile = new File(contextFileName);

        if (!ctxFile.exists()) {

            // Create context and serialize
            rootContext = new Context(rng);
            rootContext.fromFileNames(inputFileNames);

            // Add some dummy values to the context
            rootContext.addDefaultValue(rootContext.getTypeByName("Byte"), "(0x48.toByte())");
            rootContext.addDefaultValue(rootContext.getTypeByName("Float"), "1.0f");
            rootContext.addDefaultValue(rootContext.getTypeByName("Double"), "2.0");
            rootContext.addDefaultValue(rootContext.getTypeByName("Int"), "3");
            rootContext.addDefaultValue(rootContext.getTypeByName("Short"), "(4.toShort())");
            rootContext.addDefaultValue(rootContext.getTypeByName("Long"), "(5.toLong())");
            rootContext.addDefaultValue(rootContext.getTypeByName("Boolean"), "true");
            rootContext.addDefaultValue(rootContext.getTypeByName("Boolean"), "false");
            rootContext.addDefaultValue(rootContext.getTypeByName("String"), "\"fooBar\"");
            rootContext.addDefaultValue(rootContext.getTypeByName("Char"), "'w'");

            FileOutputStream f = new FileOutputStream(contextFileName);
            ObjectOutputStream o = new ObjectOutputStream(f);

            // Serialize file
            o.writeObject(rootContext);

            o.close();
            f.close();

        } else {

            // Deserialize
            FileInputStream fi = new FileInputStream(contextFileName);
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects
            rootContext = (Context) oi.readObject();

            oi.close();
            fi.close();
        }

    }

    private List<Context> createContexts() {
        List<Context> ctxs = new ArrayList<>();

        for (int i = 0; i < numberOfFiles; i++) {
            ctxs.add(rootContext.clone());
        }

        return ctxs;
    }

    public void run() throws IOException, InterruptedException {
        List<Context> contexts = createContexts();

        ASTNode grammarRoot = new GrammarTransformer(lexerGrammar, parserGrammar).transformGrammar();
        FuzzerStatistics stats = new FuzzerStatistics();

        grammarRoot.recordStatistics(stats);

        for (int i = 0; i < numberOfFiles; i++) {
            CodeFragment code = new CodeFragment();

            for (int j = 0; j < numberOfStatements; j++) {
                // Function declarations.
                ASTNode nodeToSample = grammarRoot.getChildren().get(0).getChildren().get(5).getChildren().get(0).getChildren().get(0).getChildren().get(2);

                CodeFragment newCode = nodeToSample.getSample(rng, contexts.get(i));
                code.extend(newCode);
            }
            String randomFileName = UUID.randomUUID().toString();
            String outputFileName = directoryOutput + randomFileName;

            String text = "fun main(args: Array<String>) {\n";
            text += code.getText();
            text += "\n}";

            String kotlinFile = outputFileName + ".kt";

            BufferedWriter writer = new BufferedWriter(new FileWriter(kotlinFile));
            writer.write(text);
            writer.close();

            KotlinParseTree parseTree = parseKotlinCode(tokenizeKotlinCode(text));
            stats.record(parseTree);

            int argNum = 0;
            List<Long> compilertimes = new LinkedList<>();

            // Run each of the compilers in series
            for (String compilerArgs : args) {
                argNum++;
                List<String> command = new ArrayList<>();
                File compilerOutputFile = new File(directoryOutput + "v" +  argNum + "/" + randomFileName + ".log");
                compilerOutputFile.createNewFile();

                command.add(kotlinCompilerPath);
                command.add(kotlinFile);

                List<String> inputArgs = compilerArgs.isEmpty() ? new ArrayList<>() : new ArrayList<>(List.of(compilerArgs.split(" ")));
                inputArgs.add("-d");

                // Manually define the jar output
                inputArgs.add(directoryOutput + "v" +  argNum + "/" + randomFileName + ".jar");
                command.addAll(inputArgs);

                System.out.println(command);

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(new File(System.getProperty("user.dir")));
                pb.redirectError(compilerOutputFile);

                compilertimes.add(System.currentTimeMillis());

                Process p = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder builder = new StringBuilder();

                String line;
                while ( (line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                }

                p.waitFor();
                compilertimes.set(compilertimes.size() - 1, System.currentTimeMillis() - compilertimes.get(compilertimes.size() - 1));
            }

            stats.record(compilertimes.get(0), compilertimes.get(1));

            BufferedWriter statsWriter = new BufferedWriter(new FileWriter(directoryOutput + "/" + randomFileName + ".json"));
            statsWriter.write(stats.toJson().toString());
            statsWriter.close();
        }
    }
}
