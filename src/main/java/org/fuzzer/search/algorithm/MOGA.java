package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.archive.ElitistArchive;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.MOFitnessFunction;
import org.fuzzer.search.operators.muation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;

import java.util.List;

public class MOGA extends GA {

    private final ElitistArchive elitistArchive;

    private final MOFitnessFunction f;

    public MOGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                Context rootContext, Long seed,
                Long populationSize,
                MOFitnessFunction fitnessFunction,
                SelectionOperator selectionOperator,
                MutationOperator mutationOperator,
                RecombinationOperator recombinationOperator,
                ClusteringEngine<CodeBlock> clusteringEngine,
                boolean[] shouldMinimize) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator, clusteringEngine);

        this.f = fitnessFunction;
        this.elitistArchive = new ElitistArchive(fitnessFunction, shouldMinimize);
    }

    @Override
    public List<CodeBlock> search() {
        startGlobalStats();
        List<CodeBlock> pop = getNewBlocks(populationSize);
        elitistArchive.addAll(pop, f);

        while (!exceededTimeBudget()) {
            List<CodeBlock> parents = selectParents(pop);
            List<CodeBlock> children = getOffspring(parents);
            List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

            elitistArchive.addAll(children, f);
            elitistArchive.addAll(newBlocks, f);

            updatePopulation(pop, parents, children, newBlocks);
        }

        return elitistArchive.getArchive().stream().toList();
    }
}
