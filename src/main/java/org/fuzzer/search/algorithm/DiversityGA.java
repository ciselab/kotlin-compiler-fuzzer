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

    public DiversityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize,
                       SOFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       MutationOperator mutationOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator, clusteringEngine);
    }

    @Override
    public List<CodeBlock> search() {
        startGlobalStats();
        List<CodeBlock> pop = getNewBlocks(populationSize);
        List<CodeBlock> parents;

        while (!exceededTimeBudget()) {

            long numberOfSelections = populationSize / 3L;
            //TODO: test clustering
            parents = selectionOperator.select(pop, numberOfSelections);

            List<CodeBlock> children = getOffspring(parents);
            List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

            updatePopulation(pop, parents, children, newBlocks);
        }

        return pop;
    }

    private List<Point<CodeBlock>> getPoints(List<CodeBlock> population) {
        return population.stream().map(block -> new Point<>(block, block.stats().getVisitations())).toList();
    }
}
