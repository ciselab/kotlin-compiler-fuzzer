package org.fuzzer.search.operators.recombination.suite;

import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.utils.Tuple;

public interface SuiteRecombinationOperator {
    Tuple<TestSuite, TestSuite> recombine(TestSuite s1, TestSuite s2);
}
