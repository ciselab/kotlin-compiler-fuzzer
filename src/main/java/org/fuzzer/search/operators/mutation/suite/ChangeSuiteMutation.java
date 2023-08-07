package org.fuzzer.search.operators.mutation.suite;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.operators.generator.BlockGenerator;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;

public class ChangeSuiteMutation implements SuiteMutationOperator {
    private final double mutationProbability;

    private final LinkedList<CodeBlock> codeBlockCache;

    private final BlockGenerator blockGenerator;

    private final ASTNode nodeToSample;

    private final FuzzerStatistics globalStats;

    public ChangeSuiteMutation(BlockGenerator blockGenerator, double mutationProbability,
                               ASTNode nodeToSample, FuzzerStatistics globalStats) {
        this.mutationProbability = mutationProbability;
        this.codeBlockCache = new LinkedList<>();
        this.blockGenerator= blockGenerator;
        this.nodeToSample = nodeToSample;
        this.globalStats = globalStats;
    }

    @Override
    public TestSuite mutate(TestSuite suite, RandomNumberGenerator rng) {
        if (!rng.randomBoolean(mutationProbability)) {
            return suite;
        }

        suite.remove(rng);

        if (!codeBlockCache.isEmpty()) {
            CodeBlock blockToAdd = codeBlockCache.pop();
            suite.add(blockToAdd);

            return suite;
        }

        List<CodeBlock> newBlocks = blockGenerator.generateBlocks(1, nodeToSample, globalStats);
        CodeBlock blockToAdd = newBlocks.get(0);
        newBlocks.remove(blockToAdd);

        suite.add(blockToAdd);

        codeBlockCache.addAll(newBlocks);
        return suite;
    }

    @Override
    public List<TestSuite> mutate(List<TestSuite> suites, RandomNumberGenerator rng) {
        List<TestSuite> suitesToMutate = new LinkedList<>();
        List<TestSuite> unchangedSuites = new LinkedList<>();

        for (TestSuite s : suites) {
            if (rng.randomBoolean(mutationProbability)) {
                suitesToMutate.add(s);
            } else {
                unchangedSuites.add(s);
            }
        }

        if (codeBlockCache.size() < suitesToMutate.size()) {
            List<CodeBlock> blocks = blockGenerator
                    .generateBlocks(suitesToMutate.size() - codeBlockCache.size(), nodeToSample, globalStats);
            codeBlockCache.addAll(blocks);
        }

        for (TestSuite s : suitesToMutate) {
            s.remove(rng);
            CodeBlock blockToAdd = codeBlockCache.pop();
            s.add(blockToAdd);
        }

        suitesToMutate.addAll(unchangedSuites);

        return suitesToMutate;
    }
}
