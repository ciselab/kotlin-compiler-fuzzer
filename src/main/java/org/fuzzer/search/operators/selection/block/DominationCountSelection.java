package org.fuzzer.search.operators.selection.block;

import org.fuzzer.search.archive.ElitistArchive;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.MOFitnessFunction;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.Tuple;

import java.util.*;

public class DominationCountSelection extends MOSelectionOperator {

    private final Long maximumAllowedLength;

    private final MOFitnessFunction fitnessFunction;

    private final SOSelectionOperator soSelectionOperator;

    private final boolean[] shouldMinimize;

    public DominationCountSelection(Long maximumAllowedLength, MOFitnessFunction fitnessFunction,
                                    SOSelectionOperator soSelectionOperator, boolean[] shouldMinimizeObj) {
        this.maximumAllowedLength = maximumAllowedLength;
        this.fitnessFunction = fitnessFunction;
        this.soSelectionOperator = soSelectionOperator;
        this.shouldMinimize = shouldMinimizeObj;
    }

    @Override
    SOFitnessFunction getSOFitnessFunction() {
        return soSelectionOperator.getSOFitnessFunction();
    }

    @Override
    long getMaxAllowedSize() {
        return maximumAllowedLength;
    }

    @Override
    public List<CodeBlock> select(List<CodeBlock> population, long numberOfSelections) {
       List<CodeBlock> feasibleSelections = getFeasibleSelections(population);

       PriorityQueue<Tuple<Double, Integer>> pq = evaluatePopulation(feasibleSelections);

       return soSelectionOperator.select(feasibleSelections, numberOfSelections, pq);
    }

    @Override
    public PriorityQueue<Tuple<Double, Integer>> evaluatePopulation(List<CodeBlock> feasibleSelections) {
        PriorityQueue<Tuple<Double, Integer>> pq = new PriorityQueue<>(selectionComparator);

        Map<CodeBlock, List<CodeBlock>> dominations = getDominations(feasibleSelections);

        for (int i = 0; i < feasibleSelections.size(); i++) {
            CodeBlock block = feasibleSelections.get(i);
            pq.add(new Tuple<>((double) dominations.get(block).size(), i));
        }

        return pq;
    }

    /**
     * Returns a map of each code block to the list of code blocks that it is dominated by.
     */
    @Override
    Map<CodeBlock, List<CodeBlock>> getDominations(List<CodeBlock> population) {
        Map<CodeBlock, List<CodeBlock>> dominations = new HashMap<>(population.size());

        for (CodeBlock b : population) {
            dominations.put(b, new LinkedList<>());
        }

        for (int i = 0; i < population.size(); i++) {
            for (int j = i + 1; j < population.size(); j++) {
                CodeBlock b1 = population.get(i);
                CodeBlock b2 = population.get(j);

                int domination = ElitistArchive.dominates(fitnessFunction.evaluate(b2), fitnessFunction.evaluate(b1), shouldMinimize);

                if (domination == 1) {
                    dominations.get(b1).add(b2);
                } else if (domination == -1) {
                    dominations.get(b2).add(b1);
                }
            }
        }

        return dominations;
    }
}
