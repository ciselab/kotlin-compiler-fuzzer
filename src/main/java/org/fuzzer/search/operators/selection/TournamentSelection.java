package org.fuzzer.search.operators.selection;

import org.fuzzer.representations.chromosome.CodeBlock;
import org.fuzzer.search.fitness.FitnessFunction;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class TournamentSelection implements SelectionOperator {
    private final int tournamentSize;

    private final double selectionProbability;

    private final RandomNumberGenerator rng;

    private static Comparator<Tuple<Double, Integer>> selectionComparator = new Comparator<Tuple<Double, Integer>>() {
        @Override
        public int compare(Tuple<Double, Integer> t1, Tuple<Double, Integer> t2) {
            return Double.compare(t1.first(), t2.first());
        }
    };

    private final FitnessFunction fitnessFunction;

    public TournamentSelection(int tournamentSize, double selectionProbability,
                               RandomNumberGenerator rng, FitnessFunction fitnessFunction) {
        this.tournamentSize = tournamentSize;
        this.selectionProbability = selectionProbability;
        this.rng = rng;
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    public List<CodeBlock> select(List<CodeBlock> population, int numberOfSelections) {
        List<CodeBlock> selections = new LinkedList<>();
        fitnessFunction.updatePopulation(population);

        for (int i = 0; i < numberOfSelections; i++) {
            LinkedList<CodeBlock> participants = new LinkedList<>();
            PriorityQueue<Tuple<Double, Integer>> pq = new PriorityQueue<>();

            for (int j = 0; j < tournamentSize; j++) {
                CodeBlock randomBlock = population.get(rng.fromUniformDiscrete(0, population.size() - 1));

                participants.add(randomBlock);
                pq.add(new Tuple<>(fitnessFunction.evaluate(randomBlock), j));
            }

            double selectionRoll = rng.fromUniformContinuous(0.0, 1.0);
            double p = selectionRoll;

            while (!pq.isEmpty()) {
                if (pq.size() == 1 || p < pq.peek().first()) {
                    CodeBlock selection = participants.get(pq.poll().second());
                    selections.add(selection);
                    break;
                }

                p *= (1 - selectionRoll);
                pq.poll();
            }
        }

        return selections;
    }
}
