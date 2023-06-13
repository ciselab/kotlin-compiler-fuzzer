package org.fuzzer.search.algorithm;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.operators.generator.BlockGenerator;

import java.util.*;

public abstract class Search {

    private final ASTNode nodeToSample;

    private final Long timeBudgetMilis;

    private final Context rootContext;

    private final Long seed;

    private final FuzzerStatistics globalStats;

    protected final BlockGenerator blockGenerator;

    public Search(ASTNode nodeToSample, Long timeBudgetMilis,
                  Context rootContext, Long seed) {
        this.nodeToSample = nodeToSample;
        this.timeBudgetMilis = timeBudgetMilis;
        this.rootContext = rootContext;
        this.seed = seed;

        this.globalStats = new FuzzerStatistics();
        this.blockGenerator = new BlockGenerator(rootContext, seed);
    }

    public ASTNode getNodeToSample() {
        return nodeToSample;
    }

    public FuzzerStatistics getGlobalStats() {
        return globalStats;
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

    protected List<CodeBlock> getNewBlocks(long numberOfBlocks) {
        return blockGenerator.generateBlocks(numberOfBlocks, nodeToSample, globalStats);
    }

    public abstract List<CodeBlock> search();

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
