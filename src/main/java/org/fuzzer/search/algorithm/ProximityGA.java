package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.proximity.SingularSOProximityFitnessFunction;
import org.fuzzer.search.operators.mutation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;
import org.fuzzer.utils.AsyncSnapshotWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ProximityGA extends GA {

    private final SingularSOProximityFitnessFunction fitnessFunction;

    private final Long numberOfIters;
    public ProximityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize, Long newBlocksGenerated,
                       SingularSOProximityFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       MutationOperator mutationOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine,
                       Long numberOfIterationsPerTarget,
                       Long snapshotInterval, String outputDir) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, newBlocksGenerated, fitnessFunction,
                selectionOperator, mutationOperator, recombinationOperator,
                clusteringEngine, snapshotInterval, outputDir);

        this.fitnessFunction = fitnessFunction;
        this.numberOfIters = numberOfIterationsPerTarget;
    }
    @Override
    public List<CodeBlock> search(boolean takeSnapshots) {
        startGlobalStats();
        List<CodeBlock> parents;
        while (!exceededTimeBudget()) {

            List<CodeBlock> pop = getNewBlocks(populationSize);

            for (int i = 0; i < numberOfIters && !exceededTimeBudget(); i++) {

                parents = selectParents(pop);

                List<CodeBlock> children = getOffspring(parents);
                List<CodeBlock> newBlocks = getNewBlocks(populationSize - children.size() - parents.size());

                updatePopulation(pop, new LinkedList<>(), children, newBlocks);

                if (takeSnapshots) {
                    processSnapshot(i == numberOfIters);
                }
            }

            fitnessFunction.switchTargets();
        }

        return fitnessFunction.getArchive().getArchive().stream().toList();
    }

    void processSnapshot(boolean finishedIteration) {
        if (!shouldTakeSnapshot()) {
            if (!finishedIteration) {
                return;
            }
        }

        List<CodeBlock> snapshot = takeSnapshot();
        String snapshotDir = this.outputDirectory + "snapshot-" + snapshotNumber++;
        try {
            Files.createDirectory(Paths.get(snapshotDir));
            Files.createDirectory(Paths.get(snapshotDir + "/v1"));
            Files.createDirectory(Paths.get(snapshotDir + "/v2"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AsyncSnapshotWriter snapshotWriter = new AsyncSnapshotWriter(snapshotDir, snapshot);
        snapshotWriter.start();

        updateSnapshotTime();
    }

    @Override
    List<CodeBlock> takeSnapshot() {
        return fitnessFunction.getArchive().getArchive()
                .stream().map(CodeBlock::getCopy).toList();
    }
}
