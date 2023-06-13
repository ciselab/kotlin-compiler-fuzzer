package org.fuzzer.search.fitness;

import org.fuzzer.search.chromosome.CodeBlock;

public interface MOFitnessFunction extends IndividualFitnessFunction {
    public double[] evaluate(CodeBlock block);
}
