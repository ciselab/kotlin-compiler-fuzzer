package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.operators.generator.BlockGenerator;
import org.fuzzer.search.operators.mutation.suite.SuiteMutationOperator;
import org.fuzzer.search.operators.mutation.suite.WTSMutationOperator;
import org.fuzzer.search.operators.recombination.suite.SuiteRecombinationOperator;
import org.fuzzer.search.operators.selection.suite.SuiteSOSelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;

public class ProximityWholeTestSuite extends SuiteGA {

    private final Long populationSize;

    private final Long blocksPerSuite;

    private final SuiteMutationOperator mutationOperator;

    private final RandomNumberGenerator mutationRng;

    private List<TestSuite> pop;

    public ProximityWholeTestSuite(SyntaxNode nodeToSample, Long timeBudgetMilis,
                                   Context rootContext, Long seed,
                                   Long populationSize, Long newBlocksGenerated,
                                   SuiteSOSelectionOperator selectionOperator,
                                   SuiteRecombinationOperator recombinationOperator,
                                   Long blocksPerSuite,
                                   double mutationProbability,
                                   Long snapshotInterval, String outputDir) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, newBlocksGenerated,
                selectionOperator, recombinationOperator, snapshotInterval, outputDir);
        this.populationSize = populationSize;
        this.blocksPerSuite = blocksPerSuite;
        this.mutationOperator = new WTSMutationOperator(
                new BlockGenerator(getRootContext(), getSeed()),
                mutationProbability, nodeToSample, getGlobalStats());
        this.mutationRng = new RandomNumberGenerator(getSeed());
        this.pop = new LinkedList<>();
    }

    @Override
    public List<CodeBlock> search(boolean takeSnapshots) {
        startGlobalStats();

        pop = getNewSuites(populationSize, blocksPerSuite);

        while (!exceededTimeBudget()) {

            List<TestSuite> parents = getParents(pop);
            List<TestSuite> children = getChildren(parents);
            List<TestSuite> newBlocks = getNewSuites(populationSize - children.size() - parents.size(), blocksPerSuite);

            updatePopulation(pop, parents, children, newBlocks);

            // In-place
            mutationOperator.mutate(pop, mutationRng);

            if (takeSnapshots) {
                processSnapshot();
            }
        }

        return selectionOperator.getBestSuite().getBlocks();
    }

    @Override
    List<CodeBlock> takeSnapshot() {
        return selectionOperator
                .getBestSuite().getBlocks()
                .stream().map(CodeBlock::getCopy)
                .toList();
    }
}
