package org.fuzzer.search.operators.generator;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class BlockGenerator {

    private final Context rootContext;

    private final RandomNumberGenerator seedGenerator;

    public BlockGenerator(Context rootContext, Long seed) {
        this.rootContext = rootContext;
        this.seedGenerator = new RandomNumberGenerator(seed);
    }

    private Context getNewContext() {
        Context nextCtx = rootContext.clone();
        RandomNumberGenerator nextRNG = new RandomNumberGenerator(seedGenerator.getNewSeed());
        nextCtx.updateRNG(nextRNG);

        return nextCtx;
    }

    public List<CodeBlock> generateBlocks(long numberOfBlocks, ASTNode nodeToSample, FuzzerStatistics globalStats) {
        List<CodeBlock> population = new LinkedList<>();
        while (population.size() < numberOfBlocks) {
            // Prepare a fresh context with a new seed
            Context ctx = getNewContext();
            SyntaxNode rootNode = (SyntaxNode) nodeToSample;

            // Rest statistics
            FuzzerStatistics stats = globalStats.clone();
            stats.resetVisitations();
            rootNode.recordStatistics(stats);

            List<CodeBlock> blocks = rootNode.getBlocks(ctx.getRNG(), ctx);

            population.addAll(blocks);
        }

        return population;
    }
}
