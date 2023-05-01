package org.fuzzer.search.operators.selection;

import org.fuzzer.search.archive.ElitistArchive;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.MOFitnessFunction;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.Tuple;

import java.util.*;

public class DominationRankSelection extends MOSelectionOperator {


    protected static final Comparator<Tuple<Double, Integer>> selectionComparator = new Comparator<Tuple<Double, Integer>>() {
        @Override
        public int compare(Tuple<Double, Integer> t1, Tuple<Double, Integer> t2) {
            return Double.compare(t2.first(), t1.first());
        }
    };

    private final Long maximumAllowedLength;

    private MOFitnessFunction fitnessFunction;

    private SOSelectionOperator soSelectionOperator;

    private boolean[] shouldMinimize;

    public DominationRankSelection(Long maximumAllowedLength, MOFitnessFunction fitnessFunction,
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

    private List<List<CodeBlock>> rankByDomination(List<CodeBlock> population) {
        List<List<CodeBlock>> ranks = new LinkedList<>();
        Map<CodeBlock, List<CodeBlock>> dominations = getDominations(population);

        while(!dominations.isEmpty()) {
            List<CodeBlock> rank = new LinkedList<>();
            for (CodeBlock block : dominations.keySet()) {
                if (dominations.get(block).isEmpty()) {
                    rank.add(block);
                }
            }
            for (CodeBlock block : rank) {
                dominations.remove(block);
            }
            for (CodeBlock block : dominations.keySet()) {
                dominations.get(block).removeAll(rank);
            }
            ranks.add(rank);
        }

        return ranks;
    }

    @Override
    public List<CodeBlock> select(List<CodeBlock> population, long numberOfSelections) {
        List<CodeBlock> selections = new LinkedList<>();

        List<CodeBlock> feasibleSelections = getFeasibleSelections(population);
        List<List<CodeBlock>> rankedSolutions = rankByDomination(feasibleSelections);

        while (!rankedSolutions.isEmpty() && selections.size() < numberOfSelections) {
            List<CodeBlock> nextRank = rankedSolutions.remove(0);
            if (selections.size() + nextRank.size() <= numberOfSelections) {
                selections.addAll(nextRank);
            } else {
                selections.addAll(nextRank.subList(0, (int) (numberOfSelections - selections.size())));
            }
        }

        return selections;
    }

    @Override
    public PriorityQueue<Tuple<Double, Integer>> evaluatePopulation(List<CodeBlock> feasibleSelections) {
        return null;
    }

    public void setFitnessFunction(MOFitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    public void setSoSelectionOperator(SOSelectionOperator soSelectionOperator) {
        this.soSelectionOperator = soSelectionOperator;
    }

    public void setShouldMinimize(boolean[] shouldMinimize) {
        this.shouldMinimize = shouldMinimize;
    }
}
