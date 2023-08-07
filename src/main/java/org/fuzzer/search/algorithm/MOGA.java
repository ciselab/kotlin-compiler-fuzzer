package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.archive.ElitistArchive;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.MOFitnessFunction;
import org.fuzzer.search.operators.mutation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;

import java.util.List;

public class MOGA extends GA {

    private final ElitistArchive elitistArchive;

    private final MOFitnessFunction f;

    public MOGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                Context rootContext, Long seed,
                Long populationSize, Long newBlocksGenerated,
                MOFitnessFunction fitnessFunction,
                SelectionOperator selectionOperator,
                MutationOperator mutationOperator,
                RecombinationOperator recombinationOperator,
                ClusteringEngine<CodeBlock> clusteringEngine,
                boolean[] shouldMinimize,
                Long snapshotInterval, String outputDir) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, newBlocksGenerated, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator,
                clusteringEngine, snapshotInterval, outputDir);

        this.f = fitnessFunction;
        this.elitistArchive = new ElitistArchive(fitnessFunction, shouldMinimize);
    }

    @Override
    public List<CodeBlock> search(boolean takeSnapshots) {
        startGlobalStats();
        List<CodeBlock> pop = getNewBlocks(populationSize);
        elitistArchive.addAll(pop, f);

        while (!exceededTimeBudget()) {
            if (takeSnapshots) {
                processSnapshot();
            }

            List<CodeBlock> parents = selectParents(pop);
            List<CodeBlock> children = getOffspring(parents);
            List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

            elitistArchive.addAll(children, f);
            elitistArchive.addAll(newBlocks, f);

            updatePopulation(pop, parents, children, newBlocks);
        }

        return getArchivedBlocks();
    }

    @Override
    List<CodeBlock> takeSnapshot() {
        return elitistArchive.getArchive().stream().map(CodeBlock::getCopy).toList();
    }

    private List<CodeBlock> getArchivedBlocks() {
        return elitistArchive.getArchive().stream().toList();
    }
}
