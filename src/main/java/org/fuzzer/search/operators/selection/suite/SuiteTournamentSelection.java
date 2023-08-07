package org.fuzzer.search.operators.selection.suite;

import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.search.fitness.proximity.SOPopulationFitnessFunction;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class SuiteTournamentSelection extends SuiteSOSelectionOperator {
    private final long tournamentSize;

    private final double selectionProbability;

    private final Long maximumAllowedSuiteLength;

    private RandomNumberGenerator rng;

    private SOPopulationFitnessFunction fitnessFunction;

    public SuiteTournamentSelection(Long tournamentSize, double selectionProbability, Long maximumAllowedSuiteLength,
                               RandomNumberGenerator rng, SOPopulationFitnessFunction fitnessFunction) {
        this.tournamentSize = tournamentSize;
        this.selectionProbability = selectionProbability;
        this.maximumAllowedSuiteLength = maximumAllowedSuiteLength;
        this.rng = rng;
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    SOPopulationFitnessFunction getSOFitnessFunction() {
        return fitnessFunction;
    }

    @Override
    long getMaxAllowedSuiteSize() {
        return maximumAllowedSuiteLength;
    }

    @Override
    public List<TestSuite> select(List<TestSuite> triagedPopulation, long numberOfSelections, PriorityQueue<Tuple<Double, Integer>> populationFitness) {
        List<TestSuite> selections = new LinkedList<>();
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

            if (populationFitnessList.size() == 0) {
                System.out.println("Population fitness is empty.");
            }

            for (int j = 0; j < tournamentSize; j++) {
                Tuple<Double, Integer> randomTuple = rng.selectFromList(populationFitnessList);
                pq.add(randomTuple);
            }

            double p = selectionProbability;

            while (!pq.isEmpty()) {
                if (pq.size() == 1 || p < rng.fromUniformContinuous(0.0, 1.0)) {
                    TestSuite selection = triagedPopulation.get(pq.poll().second());
                    selections.add(selection);
                    break;
                }

                p *= (1 - selectionProbability);
                pq.poll();
            }
        }

        return selections;
    }

    public void setRng(RandomNumberGenerator rng) {
        this.rng = rng;
    }

    public void setFitnessFunction(SOPopulationFitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }
}
