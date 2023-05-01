package org.fuzzer.search.operators.selection;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.utils.Tuple;

import java.util.List;
import java.util.PriorityQueue;

public abstract class SOSelectionOperator extends SelectionOperator {

    abstract List<CodeBlock> select(List<CodeBlock> triagedPopulation, long numberOfSelections, PriorityQueue<Tuple<Double, Integer>> populationFitness);

    @Override
    public PriorityQueue<Tuple<Double, Integer>> evaluatePopulation(List<CodeBlock> feasibleSelections) {
        getSOFitnessFunction().updatePopulation(feasibleSelections);

        PriorityQueue<Tuple<Double, Integer>> pq = new PriorityQueue<>(selectionComparator);

        for (int i = 0; i < feasibleSelections.size(); i++) {
            CodeBlock block = feasibleSelections.get(i);
            pq.add(new Tuple<>(getSOFitnessFunction().evaluate(block), i));
        }

        return pq;
    }

    public List<CodeBlock> select(List<CodeBlock> population, long numberOfSelections) {
        List<CodeBlock> feasibleSelections = getFeasibleSelections(population);

        PriorityQueue<Tuple<Double, Integer>> pq = evaluatePopulation(feasibleSelections);

        return select(feasibleSelections, numberOfSelections, pq);
    }

    public abstract Long getSizeMaxAllowedSize();
}
