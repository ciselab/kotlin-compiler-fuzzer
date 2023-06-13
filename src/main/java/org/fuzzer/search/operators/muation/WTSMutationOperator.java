package org.fuzzer.search.operators.muation;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.operators.generator.BlockGenerator;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public class WTSMutationOperator implements SuiteMutationOperator {

    private final InsertionSuiteMutation insertionSuiteMutation;

    private final RemovalSuiteMutation removalSuiteMutation;

    private final ChangeSuiteMutation changeSuiteMutation;

    public WTSMutationOperator(BlockGenerator generator, double mutationProbability,
                               ASTNode nodeToSample, FuzzerStatistics globalStats) {
        this.insertionSuiteMutation = new InsertionSuiteMutation(generator, mutationProbability, nodeToSample, globalStats);
        this.removalSuiteMutation = new RemovalSuiteMutation(mutationProbability);
        this.changeSuiteMutation = new ChangeSuiteMutation(generator, mutationProbability, nodeToSample, globalStats);
    }

    @Override
    public TestSuite mutate(TestSuite suite, RandomNumberGenerator rng) {
        if (rng.randomBoolean(0.33d)) {
            return insertionSuiteMutation.mutate(suite, rng);
        } else {
            if (rng.randomBoolean(0.33d)) {
                return removalSuiteMutation.mutate(suite, rng);
            } else {
                return changeSuiteMutation.mutate(suite, rng);
            }
        }
    }

    @Override
    public List<TestSuite> mutate(List<TestSuite> suites, RandomNumberGenerator rng) {
        for (TestSuite suite : suites) {
            mutate(suite, rng);
        }

        return suites;
    }
}
