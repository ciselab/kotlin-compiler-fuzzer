package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.IndividualFitnessFunction;
import org.fuzzer.search.fitness.proximity.PopulationFitnessFunction;
import org.fuzzer.search.operators.generator.SuiteGenerator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.recombination.suite.SuiteRecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;
import org.fuzzer.search.operators.selection.suite.SuiteSOSelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.LinkedList;
import java.util.List;

public abstract class SuiteGA extends Search {
    protected final long populationSize;

    protected final long newSuitesGenerated;
    protected final SuiteSOSelectionOperator selectionOperator;

    protected final SuiteRecombinationOperator recombinationOperator;

    protected final RandomNumberGenerator choiceGenerator;

    private final SuiteGenerator suiteGenerator;

    public SuiteGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
              Context rootContext, Long seed,
              Long populationSize, Long newBlocksGenerated,
              SuiteSOSelectionOperator selectionOperator,
              SuiteRecombinationOperator recombinationOperator,
              Long snapshotInterval, String outputDir) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, snapshotInterval, outputDir);

        this.populationSize = populationSize;
        this.newSuitesGenerated = newBlocksGenerated;
        this.selectionOperator = selectionOperator;
        this.recombinationOperator = recombinationOperator;
        this.choiceGenerator = new RandomNumberGenerator(getSeed());
        this.suiteGenerator = new SuiteGenerator(getRootContext(), getSeed());
    }

    protected List<TestSuite> getNewSuites(long numberOfSuites, long blocksPerSuite) {
        return suiteGenerator.generateSuites(numberOfSuites, blocksPerSuite, getNodeToSample(), getGlobalStats());
    }

    protected List<TestSuite> getParents(List<TestSuite> pop) {
        long numberOfSelections = populationSize / 2L;

        return selectionOperator.select(pop, numberOfSelections);
    }

    protected List<TestSuite> getChildren(List<TestSuite> parents) {

        List<TestSuite> children = new LinkedList<>();

        for (int i = 0; i < parents.size(); i++) {
            TestSuite parent1 = parents.get(i);
            List<TestSuite> compatibleParents = parents.stream().filter(p2 -> !parent1.equals(p2)).toList();

            if (compatibleParents.isEmpty()) {
                continue;
            }

            TestSuite parent2 = compatibleParents.get(choiceGenerator.fromUniformDiscrete(0, compatibleParents.size() - 1));

            Tuple<TestSuite, TestSuite> offspring = recombinationOperator.recombine(parent1, parent2);
            children.add(offspring.first());
            children.add(offspring.second());
        }

        return children;
    }

    protected void updatePopulation(List<TestSuite> population, List<TestSuite> selectedParents,
                                    List<TestSuite> children, List<TestSuite> newBlocks) {
        population.clear();
        population.addAll(selectedParents);
        population.addAll(children);
        population.addAll(newBlocks);
    }

    protected void updatePopulation(List<TestSuite> oldPopulation, List<TestSuite> newPopulation) {
        oldPopulation.clear();
        oldPopulation.addAll(newPopulation);
    }
}
