package org.fuzzer.dt;


import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.GrammarTransformer;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.RandomNumberGenerator;

import java.io.*;
import java.util.*;

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

    private String compilerScriptPath;

    private final String kotlinCompilerPath;

    private final List<String> args;

    private final File statsFile;

    public DTRunner(int numberOfFiles, int numberOfStatements,
                    List<String> inputFileNames, String directoryOutput,
                    String kotlinCompilerPath, List<String> commandLineArgs,
                    String compilerScriptPath,
                    int seed, int maxDepth, String contextFileName,
                    String lexerFileName, String grammarFileName,
                    boolean serializeContext) throws IOException, RecognitionException, ClassNotFoundException {
        this.numberOfFiles = numberOfFiles;
        this.numberOfStatements = numberOfStatements;
        this.directoryOutput = directoryOutput;
        this.kotlinCompilerPath = kotlinCompilerPath;
        this.compilerScriptPath = compilerScriptPath;
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

            if (serializeContext) {
                // Serialize file
                o.writeObject(rootContext);

                o.close();
                f.close();
            }
        } else {

            // Deserialize
            FileInputStream fi = new FileInputStream(contextFileName);
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects
            rootContext = (Context) oi.readObject();

            oi.close();
            fi.close();
        }

        this.statsFile = new File(directoryOutput + "/stats.csv");

        if (!statsFile.exists()) {
            statsFile.createNewFile();

            BufferedWriter statsWriter = new BufferedWriter(new FileWriter(statsFile.getAbsolutePath()));
            statsWriter.write("file,time,cls,attr,func,method,constr,simple_expr,do_while,assignment,try_catch,if_expr,elvis_op,simple_stmt,k1_exit,k1_time,k1_mem,k1_sz,k2_exit,k2_time,k2_mem,k2_sz,loc,sloc,lloc,cloc,mcc,cog,smells,cmm_ratio,mcckloc,smellskloc");
            statsWriter.flush();
            statsWriter.close();
        }
    }

    private List<Context> createContexts() {
        List<Context> ctxs = new ArrayList<>();

        for (int i = 0; i < numberOfFiles; i++) {
            ctxs.add(rootContext.clone());
        }

        return ctxs;
    }

    public void run(Long seed, Long timeLimitMs) throws IOException {
        Long startTime = System.currentTimeMillis();

        BufferedWriter statsWriter = new BufferedWriter(new FileWriter(statsFile.getAbsolutePath(), true));

        ASTNode grammarRoot = new GrammarTransformer(lexerGrammar, parserGrammar).transformGrammar();

        int i = 0;

        while (System.currentTimeMillis() - startTime < timeLimitMs) {
            CodeFragment code = new CodeFragment();

            FuzzerStatistics stats = new FuzzerStatistics();
            grammarRoot.recordStatistics(stats);

            for (int j = 0; j < numberOfStatements; j++) {
//                System.out.println("Function #" + j);
                // Function declarations.
                ASTNode nodeToSample = grammarRoot.getChildren().get(0).getChildren().get(5).getChildren().get(0).getChildren().get(0).getChildren().get(2);

                Context ctx = rootContext.clone();
                ctx.updateRNGSeed(rootContext.getNewSeed());

                CodeFragment newCode = nodeToSample.getSample(rng, ctx);
                code.extend(newCode);
            }

            Long timeTaken = System.currentTimeMillis() - startTime;

            String randomFileName = UUID.randomUUID().toString();
            String outputFileName = directoryOutput + randomFileName;

            String text = "fun main(args: Array<String>) {\n";
            text += code.getText();
            text += "\n}";

            String kotlinFile = outputFileName + ".kt";

            BufferedWriter writer = new BufferedWriter(new FileWriter(kotlinFile));
//            System.out.println("Writing to " + kotlinFile);
            writer.write(text);
            writer.close();

            Map<SampleStructure, Long> sampleStatistics = stats.getExtendedGrammarVisitations();
            StringBuilder dataToWrite = new StringBuilder(randomFileName + ","
                    + timeTaken + ",");

            for (SampleStructure s : SampleStructure.values()) {
                dataToWrite.append(sampleStatistics.getOrDefault(s, 0L)).append(",");
            }

            statsWriter.newLine();
            statsWriter.write(dataToWrite.toString());
        }

        statsWriter.newLine();
        statsWriter.flush();
        statsWriter.close();
    }
}
