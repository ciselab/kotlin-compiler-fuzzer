package org.fuzzer.search.operators.selection.suite;

import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.fitness.proximity.SOPopulationFitnessFunction;
import org.fuzzer.utils.Tuple;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


// TODO: refactor this package and the block package into a unified interface
public abstract class SuiteSelectionOperator {
    protected static final Comparator<Tuple<Double, Integer>> selectionComparator = new Comparator<Tuple<Double, Integer>>() {
        @Override
        public int compare(Tuple<Double, Integer> t1, Tuple<Double, Integer> t2) {
            return Double.compare(t1.first(), t2.first());
        }
    };

    abstract SOPopulationFitnessFunction getSOFitnessFunction();

    abstract long getMaxAllowedSuiteSize();

    public List<TestSuite> getFeasibleSelections(List<TestSuite> population) {
        List<TestSuite> feasibleSelections = population.stream().filter(x -> x.size() <= getMaxAllowedSuiteSize()).toList();

        if (feasibleSelections.isEmpty()) {
            System.out.println("No feasible selections found in population.");
            return population;
        }

        return feasibleSelections;
    }

    public abstract List<TestSuite> select(List<TestSuite> population, long numberOfSelections);

    public abstract PriorityQueue<Tuple<Double, Integer>> evaluatePopulation(List<TestSuite> feasibleSelections);
}
