package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.clustering.*;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.search.operators.muation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;

import java.util.*;

public class DiversityGA extends GA {
    private List<CodeBlock> pop;

    public DiversityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize,
                       SOFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       MutationOperator mutationOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine,
                       Long snapshotInterval) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator,
                clusteringEngine, snapshotInterval);
        this.pop = new LinkedList<>();
    }

    @Override
    public List<CodeBlock> search() {
        startGlobalStats();
        pop = getNewBlocks(populationSize);

        List<CodeBlock> parents;
        List<CodeBlock> bestPop = new LinkedList<>();

        while (!exceededTimeBudget()) {

            processSnapshot();

            if (cumulativeFitness(pop) < cumulativeFitness(bestPop)) {
                bestPop.clear();
                for (CodeBlock block : pop) {
                    bestPop.add(block.getCopy());
                }
            }

            long numberOfSelections = populationSize / 3L;
            //TODO: test clustering
            parents = selectionOperator.select(pop, numberOfSelections);

            List<CodeBlock> children = getOffspring(parents);
            List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

            updatePopulation(pop, parents, children, newBlocks);
        }

        return pop;
    }

    @Override
    List<CodeBlock> takeSnapshot() {
        List<CodeBlock> popCopy = new LinkedList<>();

        for (CodeBlock block : pop) {
            popCopy.add(block.getCopy());
        }

        return popCopy;
    }

    private List<Point<CodeBlock>> getPoints(List<CodeBlock> population) {
        return population.stream().map(block -> new Point<>(block, block.stats().getVisitations())).toList();
    }

    private double cumulativeFitness(List<CodeBlock> pop) {
        double cumulativeFitness = 0.0;
        for (CodeBlock block : pop) {
            cumulativeFitness += ((SOFitnessFunction) fitnessFunction).evaluate(block);
        }
        return cumulativeFitness;
    }
}
