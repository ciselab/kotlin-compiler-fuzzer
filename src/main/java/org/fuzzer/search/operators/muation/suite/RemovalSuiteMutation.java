package org.fuzzer.search.operators.muation.suite;

import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public class RemovalSuiteMutation implements SuiteMutationOperator {

    private final double mutationProbability;

    public RemovalSuiteMutation(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    @Override
    public TestSuite mutate(TestSuite suite, RandomNumberGenerator rng) {
        if (suite.isEmpty() || !rng.randomBoolean(mutationProbability)) {
            return suite;
        }

        suite.remove(rng);

        return suite;
    }

    @Override
    public List<TestSuite> mutate(List<TestSuite> suites, RandomNumberGenerator rng) {
        for (TestSuite suite : suites) {
            mutate(suite, rng);
        }

        return suites;
    }
}
