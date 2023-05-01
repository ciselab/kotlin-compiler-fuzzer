package org.fuzzer.search.operators.selection;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.Tuple;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public abstract class SelectionOperator {
    protected static final Comparator<Tuple<Double, Integer>> selectionComparator = new Comparator<Tuple<Double, Integer>>() {
        @Override
        public int compare(Tuple<Double, Integer> t1, Tuple<Double, Integer> t2) {
            return Double.compare(t1.first(), t2.first());
        }
    };

    abstract SOFitnessFunction getSOFitnessFunction();

    abstract long getMaxAllowedSize();

    public List<CodeBlock> getFeasibleSelections(List<CodeBlock> population) {
        List<CodeBlock> feasibleSelections = population.stream().filter(x -> x.size() <= getMaxAllowedSize()).toList();

        if (feasibleSelections.isEmpty()) {
            throw new IllegalStateException("No feasible selections found in population.");
        }

        return feasibleSelections;
    }

    public abstract List<CodeBlock> select(List<CodeBlock> population, long numberOfSelections);

    public abstract PriorityQueue<Tuple<Double, Integer>> evaluatePopulation(List<CodeBlock> feasibleSelections);

}
