package org.fuzzer.search.fitness;

import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.search.chromosome.CodeBlock;

import java.util.List;

public class DiversityFitnessFunction implements FitnessFunction {

    private List<CodeBlock> population;

    private final DistanceMetric distanceMetric;

    private final static SampleStructure[] features = SampleStructure.values();

    public DiversityFitnessFunction(List<CodeBlock> population, DistanceMetric distanceMetric) {
        this.population = population;
        this.distanceMetric = distanceMetric;
    }

    @Override
    public void updatePopulation(List<CodeBlock> population) {
        this.population = population;
    }

    private double similarity(CodeBlock b1, CodeBlock b2) {
        double sum = 0.0;

        switch (distanceMetric) {
            case EUCLIDEAN -> {
                for (SampleStructure feature : features) {
                    double diff = b1.getNumberOfSamples(feature) - b1.getNumberOfSamples(feature);
                    sum += Math.sqrt(diff * diff);
                }
            }
            case MANHATTAN -> {
                for (SampleStructure feature : features) {
                    sum += Math.abs(b1.getNumberOfSamples(feature) - b1.getNumberOfSamples(feature));
                }
            }
        }

        return sum;
    }

    private double maximumSimilarity(CodeBlock b, List<CodeBlock> bs) {
        double maxSim = 0.0;

        for (CodeBlock bi : bs) {
            if (bi.equals(b)) {
                continue;
            }

            maxSim = Math.max(maxSim, similarity(b, bi));
        }

        return maxSim;
    }

    @Override
    public double evaluate(CodeBlock individual) {
        return 1.0 / (1.0 + maximumSimilarity(individual, population));
    }
}
