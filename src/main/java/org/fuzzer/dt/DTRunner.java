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
import org.fuzzer.utils.AsyncSnapshotWriter;
import org.fuzzer.utils.FileUtilities;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DTRunner {
    private final String directoryOutput;

    private final LexerGrammar lexerGrammar;

    private final Grammar parserGrammar;

    // Unused for now
    private final Context rootContext;
    private final Configuration cfg;
    private final int searchSeed;

    private final int selectionSeed;

    private final int mutationSeed;

    private final int recombinationSeed;

    public DTRunner(List<String> inputFileNames,
                    String directoryOutput, String configFilePath,
                    int ctxSeed, int searchSeed, int selectionSeed,
                    int mutationSeed, int recombinationSeed,
                    String contextFileName, String lexerFileName,
                    String grammarFileName, boolean serializeContext)
            throws IOException, RecognitionException, ClassNotFoundException {
        this.directoryOutput = directoryOutput;
        this.searchSeed = searchSeed;
        this.selectionSeed = selectionSeed;
        this.mutationSeed = mutationSeed;
        this.recombinationSeed = recombinationSeed;

        File lexerFile = new File(lexerFileName);
        File parserFile = new File(grammarFileName);

        lexerGrammar = new LexerGrammar(FileUtilities.fileContentToString(lexerFile));
        parserGrammar = new Grammar(FileUtilities.fileContentToString(parserFile));

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
    }

    public void run(Long timeLimitMs, Long snapshotInterval, boolean takeSnapshots) {
        ASTNode grammarRoot = new GrammarTransformer(lexerGrammar, parserGrammar).transformGrammar();
        // Function declarations node
        ASTNode functionNode = grammarRoot.getChildren().get(0).getChildren().get(5).getChildren().get(0).getChildren().get(0).getChildren().get(2);

        // One or more functions
        SyntaxNode nodeToSample = new PlusNode(List.of(functionNode), cfg);

        Search searchAlgorithm = cfg.getSearchStrategy(nodeToSample, timeLimitMs, rootContext,
                searchSeed, selectionSeed, mutationSeed, recombinationSeed, snapshotInterval, directoryOutput);
        List<CodeBlock> output = searchAlgorithm.search(takeSnapshots);

        AsyncSnapshotWriter blockWriter = new AsyncSnapshotWriter(directoryOutput + "final", output);
        blockWriter.start();
    }
}
