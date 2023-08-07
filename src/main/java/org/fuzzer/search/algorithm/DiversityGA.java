package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.clustering.*;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.search.operators.mutation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;

import java.util.*;

public class DiversityGA extends GA {
    private List<CodeBlock> pop;

    public DiversityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize, Long newBlocksGenerated,
                       SOFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       MutationOperator mutationOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine,
                       Long snapshotInterval, String outputDir) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, newBlocksGenerated, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator,
                clusteringEngine, snapshotInterval, outputDir);
        this.pop = new LinkedList<>();
    }

    @Override
    public List<CodeBlock> search(boolean takeSnapshots) {
        startGlobalStats();
        pop = getNewBlocks(populationSize);

        List<CodeBlock> parents;
        List<CodeBlock> bestPop = new LinkedList<>();

        while (!exceededTimeBudget()) {
            ((SOFitnessFunction) fitnessFunction).updatePopulation(pop);

            if (takeSnapshots) {
                processSnapshot();
            }

            if (cumulativeFitness(pop) > cumulativeFitness(bestPop)) {
                bestPop.clear();
                for (CodeBlock block : pop) {
                    bestPop.add(block.getCopy());
                }
            }

            parents = selectParents(pop);

            List<CodeBlock> children = getOffspring(parents);
            List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size());

            updatePopulation(pop, new LinkedList<>(), children, newBlocks);
        }

        return bestPop;
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
