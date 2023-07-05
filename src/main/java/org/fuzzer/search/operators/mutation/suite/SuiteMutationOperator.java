package org.fuzzer.search.operators.mutation.suite;

import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public interface SuiteMutationOperator {
    TestSuite mutate(TestSuite suite, RandomNumberGenerator rng);

    List<TestSuite> mutate(List<TestSuite> suites, RandomNumberGenerator rng);
}
