package org.fuzzer.search;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.chromosome.CodeBlock;
import org.fuzzer.representations.chromosome.CodeSnippet;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.fitness.FitnessFunction;
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

    public DiversityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       int populationSize,
                       FitnessFunction fitnessFunction, SelectionOperator selectionOperator) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed);

        this.populationSize = populationSize;
        this.seedGenerator = new RandomNumberGenerator(getSeed());
        this.globalStats = new FuzzerStatistics();
        this.selectionOperator = selectionOperator;
    }

    private Context getNewContext() {
        Context nextCtx = getRootContext().clone();
        RandomNumberGenerator nextRNG = new RandomNumberGenerator(seedGenerator.getNewSeed());
        nextCtx.updateRNG(nextRNG);

        return nextCtx;
    }

    private List<CodeBlock> initialize() {
        List<CodeBlock> population = new LinkedList<>();

        Map<String, Tuple<CodeSnippet, Set<KCallable>>> snippetTable = new HashMap<>();

        for (int i = 0; i < populationSize; i++) {
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
            Set<String> dependencyNames = snippetTable.get(snippetName)
                    .second().stream().map(KCallable::getName).collect(Collectors.toSet());

            List<CodeSnippet> dependencySnippets = new ArrayList<>(snippetTable.entrySet()
                    .stream().filter(entry -> dependencyNames.contains(entry.getKey()))
                    .map(entry -> entry.getValue().first())
                    .toList());

            // Add the generated snippet itself
            dependencySnippets.add(snippetTable.get(snippetName).first());

            CodeBlock newIndividual = new CodeBlock(dependencySnippets, snippetTable.get(snippetName).second());
            population.add(newIndividual);
        }

        return population;
    }

    @Override
    public List<Tuple<CodeFragment, FuzzerStatistics>> search() {
        globalStats.start();
        List<CodeBlock> pop = initialize();

        return pop.stream().map(block -> new Tuple<>(block.getText(), block.getStats())).toList();
    }
}
