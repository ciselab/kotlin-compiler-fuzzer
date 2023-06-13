package org.fuzzer.search.fitness;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.List;

public class SimpleSOFitness implements SOFitnessFunction {

    private final int featureToEvaluate;

    public SimpleSOFitness(int featureToEvaluate) {
        this.featureToEvaluate = featureToEvaluate;
    }

    @Override
    public double evaluate(CodeBlock individual) {
        return individual.stats().getVisitations()[featureToEvaluate];
    }

    @Override
    public void updatePopulation(List<CodeBlock> population) {

    }
}
