package org.fuzzer.dt;


import org.antlr.runtime.RecognitionException;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.GrammarTransformer;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.syntax.PlusNode;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.algorithm.Search;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private final int maxDepth;
    private final Configuration cfg;
    private final List<String> args;

    private final int searchSeed;

    private final int selectionSeed;

    private final int mutationSeed;

    private final int recombinationSeed;

    private final File statsFile;

    public DTRunner(int numberOfFiles, int numberOfStatements,
                    List<String> inputFileNames, String directoryOutput,
                    List<String> commandLineArgs,String configFilePath,
                    int ctxSeed, int searchSeed, int selectionSeed,
                    int mutationSeed, int recombinationSeed,
                    int maxDepth, String contextFileName,
                    String lexerFileName, String grammarFileName,
                    boolean serializeContext) throws IOException, RecognitionException, ClassNotFoundException {
        this.numberOfFiles = numberOfFiles;
        this.numberOfStatements = numberOfStatements;
        this.directoryOutput = directoryOutput;
        this.args = commandLineArgs;
        this.maxDepth = maxDepth;
        this.searchSeed = searchSeed;
        this.selectionSeed = selectionSeed;
        this.mutationSeed = mutationSeed;
        this.recombinationSeed = recombinationSeed;

        File lexerFile = new File(lexerFileName);
        File parserFile = new File(grammarFileName);

        lexerGrammar = new LexerGrammar(FileUtilities.fileContentToString(lexerFile));
        parserGrammar = new Grammar(FileUtilities.fileContentToString(parserFile));

        ruleHandler = new RuleHandler(lexerGrammar, parserGrammar);
        RandomNumberGenerator rng = new RandomNumberGenerator(ctxSeed);

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

            if (serializeContext) {
                FileOutputStream f = new FileOutputStream(contextFileName);
                ObjectOutputStream o = new ObjectOutputStream(f);

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

        this.cfg = new Configuration(configFilePath);
        this.statsFile = new File(directoryOutput + "/stats.csv");
    }

    private List<Context> createContexts() {
        List<Context> ctxs = new ArrayList<>();

        for (int i = 0; i < numberOfFiles; i++) {
            ctxs.add(rootContext.clone());
        }

        return ctxs;
    }

    public void run(Long timeLimitMs, Long snapshotInterval, boolean takeSnapshots) throws IOException {
        ASTNode grammarRoot = new GrammarTransformer(lexerGrammar, parserGrammar).transformGrammar();
        // Function declarations node
        ASTNode functionNode = grammarRoot.getChildren().get(0).getChildren().get(5).getChildren().get(0).getChildren().get(0).getChildren().get(2);

        // One or more functions
        SyntaxNode nodeToSample = new PlusNode(List.of(functionNode), cfg);

        Search searchAlgorithm = cfg.getSearchStrategy(nodeToSample, timeLimitMs, rootContext,
                searchSeed, selectionSeed, mutationSeed, recombinationSeed, snapshotInterval);
        List<CodeBlock> output = searchAlgorithm.search(takeSnapshots);

        // Write the statistics of the run
        BufferedWriter statsWriter = new BufferedWriter(new FileWriter(statsFile.getAbsolutePath(), true));
        if (!statsFile.exists()) {
            statsFile.createNewFile();
        }

        statsWriter.write("file,time,chars,cls,attr,func,method,constr,simple_expr,do_while,assignment,try_catch,if_expr,elvis_op,simple_stmt,k1_exit,k1_time,k1_mem,k1_sz,k2_exit,k2_time,k2_mem,k2_sz,loc,sloc,lloc,cloc,mcc,cog,smells,cmm_ratio,mcckloc,smellskloc");
        statsWriter.flush();

        for (CodeBlock code : output) {
            String randomFileName = UUID.randomUUID().toString();
            String outputFileName = directoryOutput + "/" + randomFileName + ".kt";

//            System.out.println("Writing to " + outputFileName);
//            System.out.println(new File(outputFileName).isFile());
//            System.out.println(new File(directoryOutput).isDirectory());

            String text = "fun main(args: Array<String>) {\n";
            text += code.text();
            text += "\n}";

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write(text);
            writer.close();

            statsWriter.newLine();
            statsWriter.write(randomFileName + "," + code.stats().csv());
        }

        statsWriter.newLine();
        statsWriter.flush();
        statsWriter.close();

        if (takeSnapshots) {
            processSnapshots(searchAlgorithm, snapshotInterval);
        }
    }

    private void processSnapshots(Search searchAlgorithm, Long snapshotInterval) throws IOException {
        List<List<CodeBlock>> snapshots = searchAlgorithm.getSnapshots();
        int iter = 1;

        for (List<CodeBlock> snapshot : snapshots) {
            String snapshotDir = directoryOutput +
                    "snapshots-" + iter++ * snapshotInterval / 1000;
            Files.createDirectory(Paths.get(snapshotDir));
            Files.createDirectory(Paths.get(snapshotDir + "/v1"));
            Files.createDirectory(Paths.get(snapshotDir + "/v2"));

            for (CodeBlock code : snapshot) {
                String randomFileName = UUID.randomUUID().toString();
                String outputFileName = snapshotDir + "/" + randomFileName + ".kt";

                String text = "fun main(args: Array<String>) {\n";
                text += code.text();
                text += "\n}";

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
                writer.write(text);
                writer.close();
            }
        }
    }
}
