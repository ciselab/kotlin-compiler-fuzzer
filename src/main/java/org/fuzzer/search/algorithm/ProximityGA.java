package org.fuzzer.search.algorithm;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.proximity.SingularSOProximityFitnessFunction;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;
import org.fuzzer.utils.Tuple;

import java.util.List;

public class ProximityGA extends GA {

    private final SingularSOProximityFitnessFunction fitnessFunction;

    private final Long numberOfIters;
    public ProximityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize,
                       SingularSOProximityFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine,
                       Long numberOfIterationsPerTarget) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, fitnessFunction,
                selectionOperator, recombinationOperator, clusteringEngine);

        this.fitnessFunction = fitnessFunction;
        this.numberOfIters = numberOfIterationsPerTarget;
    }
    @Override
    public List<Tuple<CodeFragment, FuzzerStatistics>> search() {
        startGlobalStats();
        List<CodeBlock> pop = getNewBlocks(populationSize);
        List<CodeBlock> parents;

        while (!exceededTimeBudget()) {

            for (int i = 0; i < numberOfIters; i++) {
                long numberOfSelections = populationSize / 4L;

                parents = selectionOperator.select(pop, numberOfSelections);

                List<CodeBlock> children = getChildren(parents);
                List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

                updatePopulation(pop, parents, children, newBlocks);
            }

            fitnessFunction.switchTargets();
        }

        return fitnessFunction
                .getArchive().getArchive()
                .stream().map(block -> new Tuple<>(block.getText(), block.getStats())).toList();
    }
}
