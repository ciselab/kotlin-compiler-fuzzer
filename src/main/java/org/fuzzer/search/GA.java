package org.fuzzer.search;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.FitnessFunction;
import org.fuzzer.search.operators.recombination.RecombinationOperator;
import org.fuzzer.search.operators.selection.SelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;

public abstract class GA extends Search {

    protected final long populationSize;

    protected final SelectionOperator selectionOperator;

    protected final RecombinationOperator recombinationOperator;

    protected final RandomNumberGenerator choiceGenerator;

    protected final ClusteringEngine<CodeBlock> clusteringEngine;
    public GA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                Context rootContext, Long seed,
                Long populationSize,
                FitnessFunction fitnessFunction,
                SelectionOperator selectionOperator,
                RecombinationOperator recombinationOperator,
                ClusteringEngine<CodeBlock> clusteringEngine) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed);

        this.populationSize = populationSize;
        this.selectionOperator = selectionOperator;
        this.recombinationOperator = recombinationOperator;
        this.choiceGenerator = new RandomNumberGenerator(getSeed());
        this.clusteringEngine = clusteringEngine;
    }

    protected List<CodeBlock> getParents(List<CodeBlock> pop) {
        Long numberOfSelections = populationSize / 4L;
        List<CodeBlock> parents;

        if (clusteringEngine != null) {
            throw new IllegalArgumentException("Clustering is not supported yet.");
        } else {
            parents = selectionOperator.select(pop, numberOfSelections);
        }

        return parents;
    }

    protected List<CodeBlock> getChildren(List<CodeBlock> parents) {

        List<CodeBlock> children = new LinkedList<>();

        for (int i = 0; i < parents.size(); i++) {
            CodeBlock parent1 = parents.get(i);
            List<CodeBlock> compatibleParents = parents.stream().filter(parent1::isCompatible).toList();

            if (compatibleParents.isEmpty()) {
                continue;
            }

            CodeBlock parent2 = compatibleParents.get(choiceGenerator.fromUniformDiscrete(0, compatibleParents.size() - 1));

            CodeBlock child = recombinationOperator.combine(parent1, parent2);
            children.add(child);
        }

        return children;
    }

    protected void updatePopulation(List<CodeBlock> population, List<CodeBlock> selectedParents,
                                    List<CodeBlock> children, List<CodeBlock> newBlocks) {
        population.clear();
        population.addAll(selectedParents);
        population.addAll(children);
        population.addAll(newBlocks);
    }
}
