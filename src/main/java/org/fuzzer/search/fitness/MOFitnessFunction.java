package org.fuzzer.search.fitness;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.List;

public interface MOFitnessFunction extends FitnessFunction {
    public double[] evaluate(CodeBlock block);
}
