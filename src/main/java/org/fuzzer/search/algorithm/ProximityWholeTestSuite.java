package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.operators.generator.BlockGenerator;
import org.fuzzer.search.operators.muation.suite.SuiteMutationOperator;
import org.fuzzer.search.operators.muation.suite.WTSMutationOperator;
import org.fuzzer.search.operators.recombination.suite.SuiteRecombinationOperator;
import org.fuzzer.search.operators.selection.suite.SuiteSOSelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public class ProximityWholeTestSuite extends SuiteGA {

    private final Long populationSize;

    private final Long blocksPerSuite;

    private final SuiteMutationOperator mutationOperator;

    private final RandomNumberGenerator mutationRng ;

    public ProximityWholeTestSuite(SyntaxNode nodeToSample, Long timeBudgetMilis,
                                   Context rootContext, Long seed,
                                   Long populationSize,
                                   SuiteSOSelectionOperator selectionOperator,
                                   SuiteRecombinationOperator recombinationOperator,
                                   Long blocksPerSuite,
                                   double mutationProbability) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize,
                selectionOperator, recombinationOperator);
        this.populationSize = populationSize;
        this.blocksPerSuite = blocksPerSuite;
        this.mutationOperator = new WTSMutationOperator(
                new BlockGenerator(getRootContext(), getSeed()),
                mutationProbability, nodeToSample, getGlobalStats());
        this.mutationRng = new RandomNumberGenerator(getSeed());
    }

    @Override
    public List<CodeBlock> search() {
        startGlobalStats();

        List<TestSuite> pop = getNewSuites(populationSize, blocksPerSuite);

        while (!exceededTimeBudget()) {
            List<TestSuite> parents = getParents(pop);
            List<TestSuite> children = getChildren(parents);
            List<TestSuite> newBlocks = getNewSuites(populationSize - children.size() - parents.size(), blocksPerSuite);

            updatePopulation(pop, parents, children, newBlocks);
            updatePopulation(pop, mutationOperator.mutate(pop, mutationRng));
        }

        return selectionOperator.getBestSuite().getBlocks();
    }
}
