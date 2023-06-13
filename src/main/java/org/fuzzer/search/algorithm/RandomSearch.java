package org.fuzzer.search.algorithm;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeConstruct;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RandomSearch extends Search {

    public RandomSearch(ASTNode nodeToSample, Long timeBudgetMilis,
                        Context rootContext, Long seed) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed);
    }

    @Override
    public List<CodeBlock> search() {
        long startTime = System.currentTimeMillis();
        List<CodeBlock> blocks = new LinkedList<>();
        RandomNumberGenerator seedGenerator = new RandomNumberGenerator(getSeed());
        FuzzerStatistics statistics = new FuzzerStatistics();

        while (System.currentTimeMillis() - startTime < getTimeBudgetMilis()) {
//            System.out.println("Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
            // Prepare a fresh context with a new seed
            Context nextCtx = getRootContext().clone();
            RandomNumberGenerator nextRNG = new RandomNumberGenerator(seedGenerator.getNewSeed());
            nextCtx.updateRNG(nextRNG);

            // Prepare dependency set and record statistics

            getNodeToSample().recordStatistics(statistics);

            // Sample the node.
            CodeConstruct b = getNodeToSample().getSample(nextRNG, nextCtx);

            if (!(b instanceof CodeBlock block)) {
                throw new IllegalStateException("Only block sampling is supported at the moment.");
            }

            statistics.stop();

            blocks.add(block);
            statistics.resetVisitations();
        }

        return blocks;
    }
}
