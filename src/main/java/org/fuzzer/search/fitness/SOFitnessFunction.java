package org.fuzzer.search.fitness;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.List;

public interface SOFitnessFunction extends FitnessFunction {
    double evaluate(CodeBlock individual);

    void updatePopulation(List<CodeBlock> population);
}
