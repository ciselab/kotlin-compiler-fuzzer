package org.fuzzer.search;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.chromosome.CodeBlock;
import org.fuzzer.representations.chromosome.CodeSnippet;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.fitness.FitnessFunction;
import org.fuzzer.search.operators.recombination.RecombinationOperator;
import org.fuzzer.search.operators.selection.SelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class DiversityGA extends Search {

    private final int populationSize;
    private final RandomNumberGenerator seedGenerator;

    private final FuzzerStatistics globalStats;

    private final SelectionOperator selectionOperator;

    private final RecombinationOperator recombinationOperator;

    public DiversityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       int populationSize,
                       FitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       RecombinationOperator recombinationOperator) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed);

        this.populationSize = populationSize;
        this.seedGenerator = new RandomNumberGenerator(getSeed());
        this.globalStats = new FuzzerStatistics();
        this.selectionOperator = selectionOperator;
        this.recombinationOperator = recombinationOperator;
    }

    private Context getNewContext() {
        Context nextCtx = getRootContext().clone();
        RandomNumberGenerator nextRNG = new RandomNumberGenerator(seedGenerator.getNewSeed());
        nextCtx.updateRNG(nextRNG);

        return nextCtx;
    }

    private List<CodeBlock> getRandomBlocks(int numberOfBlocks) {
        List<CodeBlock> population = new LinkedList<>();

        Map<String, Tuple<CodeSnippet, Set<KCallable>>> snippetTable = new HashMap<>();

        while(snippetTable.size() < numberOfBlocks) {
            // Prepare a fresh context with a new seed
            Context ctx = getNewContext();
            SyntaxNode rootNode = (SyntaxNode) getNodeToSample();

            // Rest statistics
            FuzzerStatistics stats = globalStats.clone();
            stats.resetVisitations();
            rootNode.recordStatistics(stats);

            List<CodeSnippet> snippets = rootNode.getSnippets(ctx.getRNG(), ctx);

            List<Tuple<CodeSnippet, Set<KCallable>>> snippetsAndDependencies = ctx.getAllSnippetCombinations(snippets);

            for (Tuple<CodeSnippet, Set<KCallable>> tup : snippetsAndDependencies) {
                snippetTable.put(tup.first().name(), tup);
            }
        }

        for (String snippetName : snippetTable.keySet()) {
            // Get the list of dependencies for the current snippet
            Set<String> dependencyNames = snippetTable.get(snippetName)
                    .second().stream().map(KCallable::getName).collect(Collectors.toSet());

            List<CodeSnippet> dependencySnippets = new ArrayList<>(snippetTable.entrySet()
                    .stream().filter(entry -> dependencyNames.contains(entry.getKey()))
                    .map(entry -> entry.getValue().first())
                    .toList());

            // Add the generated snippet itself
            dependencySnippets.add(snippetTable.get(snippetName).first());

            CodeBlock newIndividual = new CodeBlock(snippetName, dependencySnippets, snippetTable.get(snippetName).second());
            population.add(newIndividual);
        }

        return population;
    }

    @Override
    public List<Tuple<CodeFragment, FuzzerStatistics>> search() {
        globalStats.start();
        List<CodeBlock> pop = getRandomBlocks(populationSize);

        while (System.currentTimeMillis() - globalStats.getStartTime() < getTimeBudgetMilis()) {
            List<CodeBlock> parents = selectionOperator.select(pop, populationSize / 2);
            List<CodeBlock> children = new LinkedList<>();

            for (int i = 0; i < parents.size(); i += 2) {
                CodeBlock parent1 = parents.get(i);
                CodeBlock parent2 = parents.get(i + 1);

                CodeBlock child = recombinationOperator.combine(parent1, parent2);
                children.add(child);
            }

            List<CodeBlock> newBlocks = getRandomBlocks(populationSize - children.size() - parents.size());

            pop.clear();
            pop.addAll(parents);
            pop.addAll(children);
            pop.addAll(newBlocks);
        }

        return pop.stream().map(block -> new Tuple<>(block.getText(), block.getStats())).toList();
    }
}
