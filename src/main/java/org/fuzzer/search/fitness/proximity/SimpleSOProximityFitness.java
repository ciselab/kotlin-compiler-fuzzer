package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.Point;
import org.fuzzer.search.fitness.DistanceMetric;
import org.fuzzer.search.fitness.SOFitnessFunction;

import java.util.List;

public class SimpleSOProximityFitness implements SOFitnessFunction {
    private double[] target;

    public SimpleSOProximityFitness(double[] target) {
        this.target = target;
    }

    public double evaluate(double[] embedding) {
        return Point.distance(embedding, target, DistanceMetric.EUCLIDEAN);
    }

    @Override
    public double evaluate(CodeBlock individual) {
        throw new IllegalStateException("The MO proximity fitness should perform the embedding.");
    }

    @Override
    public void updatePopulation(List<CodeBlock> population) {

    }
}
