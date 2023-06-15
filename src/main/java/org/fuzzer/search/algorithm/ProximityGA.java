package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.proximity.SingularSOProximityFitnessFunction;
import org.fuzzer.search.operators.muation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;

import java.util.List;

public class ProximityGA extends GA {

    private final SingularSOProximityFitnessFunction fitnessFunction;

    private final Long numberOfIters;
    public ProximityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize,
                       SingularSOProximityFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       MutationOperator mutationOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine,
                       Long numberOfIterationsPerTarget) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator, clusteringEngine);

        this.fitnessFunction = fitnessFunction;
        this.numberOfIters = numberOfIterationsPerTarget;
    }
    @Override
    public List<CodeBlock> search() {
        startGlobalStats();
        List<CodeBlock> pop = getNewBlocks(populationSize);
        List<CodeBlock> parents;

        while (!exceededTimeBudget()) {

            for (int i = 0; i < numberOfIters; i++) {
                long numberOfSelections = populationSize / 4L;

                parents = selectionOperator.select(pop, numberOfSelections);

                List<CodeBlock> children = getOffspring(parents);
                List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

                updatePopulation(pop, parents, children, newBlocks);
            }

            fitnessFunction.switchTargets();
        }

        return fitnessFunction.getArchive().getArchive().stream().toList();
    }
}
