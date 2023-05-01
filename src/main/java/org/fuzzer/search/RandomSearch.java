package org.fuzzer.search;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

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
    public List<Tuple<CodeFragment, FuzzerStatistics>> search() {
        long startTime = System.currentTimeMillis();
        List<Tuple<CodeFragment, FuzzerStatistics>> snippets = new LinkedList<>();
        RandomNumberGenerator seedGenerator = new RandomNumberGenerator(getSeed());
        FuzzerStatistics statistics = new FuzzerStatistics();

        while (System.currentTimeMillis() - startTime < getTimeBudgetMilis()) {
//            System.out.println("Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
            // Prepare a fresh context with a new seed
            Context nextCtx = getRootContext().clone();
            RandomNumberGenerator nextRNG = new RandomNumberGenerator(seedGenerator.getNewSeed());
            nextCtx.updateRNG(nextRNG);

            // Prepare dependency set and record statistics
            Set<String> snippetDependencies = new HashSet<>();

            getNodeToSample().recordStatistics(statistics);

            // Sample the node.
            CodeFragment code = getNodeToSample().getSample(nextRNG, nextCtx, snippetDependencies);

            statistics.stop();

            snippets.add(new Tuple<>(code, statistics.clone()));
            statistics.resetVisitations();
        }

        return snippets;
    }
}
