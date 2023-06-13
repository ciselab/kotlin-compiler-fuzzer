package org.fuzzer.search.fitness;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.List;

public class SizeSOFitness implements SOFitnessFunction {
    @Override
    public double evaluate(CodeBlock individual) {
        return individual.size();
    }

    @Override
    public void updatePopulation(List<CodeBlock> population) {

    }
}
