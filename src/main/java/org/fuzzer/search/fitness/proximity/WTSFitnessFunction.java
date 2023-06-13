package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.TestSuite;

public class WTSFitnessFunction implements SOPopulationFitnessFunction {
    private final CollectiveProximityFitnessFunction f;

    public WTSFitnessFunction(CollectiveProximityFitnessFunction f) {
        this.f = f;
    }

    @Override
    public double evaluate(TestSuite blocks) {
        return f.evaluate(blocks);
    }
}
