package org.fuzzer.search.operators.selection;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.FitnessFunction;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.*;

public class TournamentSelection implements SelectionOperator {
    private final long tournamentSize;

    private final double selectionProbability;

    private final Long maximumAllowedLength;

    private final RandomNumberGenerator rng;

    private static final Comparator<Tuple<Double, Integer>> selectionComparator = new Comparator<Tuple<Double, Integer>>() {
        @Override
        public int compare(Tuple<Double, Integer> t1, Tuple<Double, Integer> t2) {
            return Double.compare(t1.first(), t2.first());
        }
    };

    private final FitnessFunction fitnessFunction;

    public TournamentSelection(Long tournamentSize, double selectionProbability, Long maximumAllowedLength,
                               RandomNumberGenerator rng, FitnessFunction fitnessFunction) {
        this.tournamentSize = tournamentSize;
        this.selectionProbability = selectionProbability;
        this.maximumAllowedLength = maximumAllowedLength;
        this.rng = rng;
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    public List<CodeBlock> select(List<CodeBlock> population, long numberOfSelections) {
        List<CodeBlock> selections = new LinkedList<>();

        // Filter heuristically on length to avoid out of memory exceptions
        List<CodeBlock> feasibleSelections = population.stream().filter(x -> x.size() <= maximumAllowedLength).toList();

        if (feasibleSelections.isEmpty()) {
            throw new IllegalStateException("No feasible selections found in population.");
        }

        fitnessFunction.updatePopulation(feasibleSelections);

        for (int i = 0; i < numberOfSelections; i++) {
            LinkedList<CodeBlock> participants = new LinkedList<>();
            PriorityQueue<Tuple<Double, Integer>> pq = new PriorityQueue<>(selectionComparator);

            for (int j = 0; j < tournamentSize; j++) {
                CodeBlock randomBlock = feasibleSelections.get(rng.fromUniformDiscrete(0, feasibleSelections.size() - 1));

                participants.add(randomBlock);
                pq.add(new Tuple<>(fitnessFunction.evaluate(randomBlock), j));
            }

            double p = selectionProbability;

            while (!pq.isEmpty()) {
                if (pq.size() == 1 || p < rng.fromUniformContinuous(0.0, 1.0)) {
                    CodeBlock selection = participants.get(pq.poll().second());
                    selections.add(selection);
                    break;
                }

                p *= (1 - selectionProbability);
                pq.poll();
            }
        }

        return selections;
    }
}
