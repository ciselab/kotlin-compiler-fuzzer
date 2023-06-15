package org.fuzzer.search.algorithm;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.ClusteringEngine;
import org.fuzzer.search.fitness.IndividualFitnessFunction;
import org.fuzzer.search.operators.muation.block.MutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.selection.block.SelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.LinkedList;
import java.util.List;

public abstract class GA extends Search {

    protected final long populationSize;

    protected final SelectionOperator selectionOperator;

    protected final RecombinationOperator recombinationOperator;

    protected final RandomNumberGenerator choiceGenerator;

    protected final MutationOperator mutationOperator;

    protected final ClusteringEngine<CodeBlock> clusteringEngine;

    protected final IndividualFitnessFunction fitnessFunction;

    public GA(SyntaxNode nodeToSample, Long timeBudgetMillis,
                Context rootContext, Long seed,
                Long populationSize,
                IndividualFitnessFunction fitnessFunction,
                SelectionOperator selectionOperator,
                MutationOperator mutationOperator,
                RecombinationOperator recombinationOperator,
                ClusteringEngine<CodeBlock> clusteringEngine,
                Long snapshotInterval) {
        super(nodeToSample, timeBudgetMillis, rootContext, seed, snapshotInterval);

        this.populationSize = populationSize;
        this.fitnessFunction = fitnessFunction;
        this.selectionOperator = selectionOperator;
        this.mutationOperator = mutationOperator;
        this.recombinationOperator = recombinationOperator;
        this.choiceGenerator = new RandomNumberGenerator(getSeed());
        this.clusteringEngine = clusteringEngine;
    }

    protected List<CodeBlock> selectParents(List<CodeBlock> pop) {
        long numberOfSelections = populationSize / 4L;
        List<CodeBlock> parents;

        if (clusteringEngine != null) {
            throw new IllegalArgumentException("Clustering is not supported yet.");
        } else {
            parents = selectionOperator.select(pop, numberOfSelections);
        }

        return parents;
    }

    protected List<CodeBlock> getOffspring(List<CodeBlock> parents) {

        List<CodeBlock> offspring = new LinkedList<>();

        for (int i = 0; i < parents.size(); i++) {
            CodeBlock parent1 = parents.get(i);
            List<CodeBlock> compatibleParents = parents.stream().filter(parent1::isCompatible).toList();

            if (compatibleParents.isEmpty()) {
                continue;
            }

            CodeBlock parent2 = choiceGenerator.selectFromList(compatibleParents);
            Tuple<CodeBlock, CodeBlock> newOffspring = recombinationOperator.combine(parent1, parent2);

            CodeBlock o1 = mutationOperator.mutate(newOffspring.first());
            CodeBlock o2 = mutationOperator.mutate(newOffspring.second());

            offspring.add(o1);
            offspring.add(o2);
        }

        return offspring;
    }

    protected void updatePopulation(List<CodeBlock> population, List<CodeBlock> selectedParents,
                                    List<CodeBlock> children, List<CodeBlock> newBlocks) {
        population.clear();
        population.addAll(selectedParents);
        population.addAll(children);
        population.addAll(newBlocks);
    }
}
