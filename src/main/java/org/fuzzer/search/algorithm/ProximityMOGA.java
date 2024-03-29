package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.proximity.ProximityMOFitnessFunction;
import org.fuzzer.search.operators.mutation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;

public class ProximityMOGA extends MOGA {
    public ProximityMOGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize, Long newBlocksGenerated,
                       ProximityMOFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       MutationOperator mutationOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine,
                       boolean[] shouldMinimize,
                       Long snapshotInterval, String outputDir) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, newBlocksGenerated, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator, clusteringEngine,
                shouldMinimize, snapshotInterval, outputDir);
    }
}
