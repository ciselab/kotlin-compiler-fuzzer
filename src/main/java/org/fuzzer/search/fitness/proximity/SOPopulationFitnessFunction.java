package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;

import java.util.Collection;

public interface SOPopulationFitnessFunction extends PopulationFitnessFunction {
    double evaluate(TestSuite blocks);
}
