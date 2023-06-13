package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.TestSuite;

public interface MOPopulationFitnessFunction {
    double[] evaluate(TestSuite blocks);
}
