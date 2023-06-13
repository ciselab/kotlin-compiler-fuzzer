package org.fuzzer.search.operators.selection.suite;

import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.utils.Tuple;

import java.util.List;
import java.util.PriorityQueue;

public abstract class SuiteSOSelectionOperator extends SuiteSelectionOperator {

    private Tuple<TestSuite, Double> bestSuite = new Tuple<>(null, Double.MAX_VALUE);

    abstract List<TestSuite> select(List<TestSuite> triagedPopulation, long numberOfSelections, PriorityQueue<Tuple<Double, Integer>> populationFitness);

    @Override
    public PriorityQueue<Tuple<Double, Integer>> evaluatePopulation(List<TestSuite> feasibleSelections) {
        PriorityQueue<Tuple<Double, Integer>> pq = new PriorityQueue<>(selectionComparator);

        for (int i = 0; i < feasibleSelections.size(); i++) {
            TestSuite suite = feasibleSelections.get(i);
            pq.add(new Tuple<>(getSOFitnessFunction().evaluate(suite), i));
        }

        return pq;
    }

    public List<TestSuite> select(List<TestSuite> population, long numberOfSelections) {
        List<TestSuite> feasibleSelections = getFeasibleSelections(population);

        PriorityQueue<Tuple<Double, Integer>> pq = evaluatePopulation(feasibleSelections);

        Tuple<Double, Integer> bestEval = pq.peek();
        if (bestEval != null && (bestSuite == null || bestEval.first() < bestSuite.second())) {
            bestSuite = new Tuple<>(feasibleSelections.get(bestEval.second()), bestEval.first());
        }

        return select(feasibleSelections, numberOfSelections, pq);
    }

    public TestSuite getBestSuite() {
        return bestSuite.first();
    }
}
