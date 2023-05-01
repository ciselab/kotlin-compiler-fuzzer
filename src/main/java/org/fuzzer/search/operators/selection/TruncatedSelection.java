package org.fuzzer.search.operators.selection;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.Tuple;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class TruncatedSelection extends SOSelectionOperator {

    private final Long maximumAllowedLength;

    private final double proportion;

    private SOFitnessFunction fitnessFunction;

    public TruncatedSelection(double proportion, Long maximumAllowedLength,
                              SOFitnessFunction fitnessFunction) {
        this.proportion = proportion;
        this.maximumAllowedLength = maximumAllowedLength;
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
        int numberOfUniqueSelections = (int) Math.floor(this.proportion * numberOfSelections);

        for (int i = 0; i < numberOfSelections; i++) {
            Tuple<Double, Integer> selection = populationFitness.poll();

            for (int j = 0; j < numberOfUniqueSelections; j++) {
                selections.add(triagedPopulation.get(selection.second()));
            }
        }

        return selections;
    }

    @Override
    public Long getSizeMaxAllowedSize() {
        return maximumAllowedLength;
    }

    public void setFitnessFunction(SOFitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }
}
