package org.fuzzer.search;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Search {

    private final ASTNode nodeToSample;

    private final Long timeBudgetMilis;

    private final Context rootContext;

    private final Long seed;

    private final RandomNumberGenerator seedGenerator;

    private final FuzzerStatistics globalStats;

    public Search(ASTNode nodeToSample, Long timeBudgetMilis,
                  Context rootContext, Long seed) {
        this.nodeToSample = nodeToSample;
        this.timeBudgetMilis = timeBudgetMilis;
        this.rootContext = rootContext;
        this.seed = seed;

        this.seedGenerator = new RandomNumberGenerator(getSeed());
        this.globalStats = new FuzzerStatistics();
    }

    public ASTNode getNodeToSample() {
        return nodeToSample;
    }

    public Long getTimeBudgetMilis() {
        return timeBudgetMilis;
    }

    public Context getRootContext() {
        return rootContext;
    }

    public Long getSeed() {
        return seed;
    }

    public abstract List<Tuple<CodeFragment, FuzzerStatistics>> search();

    protected Context getNewContext() {
        Context nextCtx = getRootContext().clone();
        RandomNumberGenerator nextRNG = new RandomNumberGenerator(seedGenerator.getNewSeed());
        nextCtx.updateRNG(nextRNG);

        return nextCtx;
    }

    protected List<CodeBlock> getRandomBlocks(long numberOfBlocks) {
        List<CodeBlock> population = new LinkedList<>();

        Map<String, Tuple<CodeSnippet, Set<KCallable>>> snippetTable = new HashMap<>();

        while (snippetTable.size() < numberOfBlocks) {
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


            // The snippet itself is also in the dependency set.
            List<CodeSnippet> dependencySnippets = new ArrayList<>(snippetTable.entrySet()
                    .stream().filter(entry -> dependencyNames.contains(entry.getKey()))
                    .map(entry -> entry.getValue().first())
                    .toList());

            CodeBlock newIndividual = new CodeBlock(snippetName, dependencySnippets, snippetTable.get(snippetName).second());
            population.add(newIndividual);
        }

        return population;
    }

    protected void startGlobalStats() {
        globalStats.start();
    }

    protected Long getStartTime() {
        return globalStats.getStartTime();
    }

    protected boolean exceededTimeBudget() {
        return System.currentTimeMillis() - getStartTime() >= getTimeBudgetMilis();
    }
}
