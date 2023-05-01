package org.fuzzer.search.operators.selection;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.*;

public class TournamentSelection extends SOSelectionOperator {
    private final long tournamentSize;

    private final double selectionProbability;

    private final Long maximumAllowedLength;

    private RandomNumberGenerator rng;

    private SOFitnessFunction fitnessFunction;

    public TournamentSelection(Long tournamentSize, double selectionProbability, Long maximumAllowedLength,
                               RandomNumberGenerator rng, SOFitnessFunction fitnessFunction) {
        this.tournamentSize = tournamentSize;
        this.selectionProbability = selectionProbability;
        this.maximumAllowedLength = maximumAllowedLength;
        this.rng = rng;
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    SOFitnessFunction getSOFitnessFunction() {
        return fitnessFunction;
    }

    @Override
    long getMaxAllowedSize() {
        return maximumAllowedLength;
    }

    @Override
    public List<CodeBlock> select(List<CodeBlock> triagedPopulation, long numberOfSelections, PriorityQueue<Tuple<Double, Integer>> populationFitness) {
        List<CodeBlock> selections = new LinkedList<>();
        List<Tuple<Double, Integer>> populationFitnessList = new LinkedList<>();

        while (!populationFitness.isEmpty()) {
            populationFitnessList.add(populationFitness.poll());
        }

        for (int i = 0; i < numberOfSelections; i++) {
            PriorityQueue<Tuple<Double, Integer>> pq = new PriorityQueue<>(new Comparator<Tuple<Double, Integer>>() {
                @Override
                public int compare(Tuple<Double, Integer> t1, Tuple<Double, Integer> t2) {
                    return t1.first().compareTo(t2.first());
                }
            });

            for (int j = 0; j < tournamentSize; j++) {
                Tuple<Double, Integer> randomTuple = rng.selectFromList(populationFitnessList);
                pq.add(randomTuple);
            }

            double p = selectionProbability;

            while (!pq.isEmpty()) {
                if (pq.size() == 1 || p < rng.fromUniformContinuous(0.0, 1.0)) {
                    CodeBlock selection = triagedPopulation.get(pq.poll().second());
                    selections.add(selection);
                    break;
                }

                p *= (1 - selectionProbability);
                pq.poll();
            }
        }

        return selections;
    }

    @Override
    public Long getSizeMaxAllowedSize() {
        return maximumAllowedLength;
    }

    public void setRng(RandomNumberGenerator rng) {
        this.rng = rng;
    }

    public void setFitnessFunction(SOFitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }
}
