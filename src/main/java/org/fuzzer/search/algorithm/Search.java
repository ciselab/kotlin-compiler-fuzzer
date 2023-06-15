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

    protected final Long snapshotInterval;

    protected Long lastSnapShotTime;

    private final List<List<CodeBlock>> snapshots;

    public Search(ASTNode nodeToSample, Long timeBudgetMilis,
                  Context rootContext, Long seed, Long snapshotInterval) {
        this.nodeToSample = nodeToSample;
        this.timeBudgetMilis = timeBudgetMilis;
        this.rootContext = rootContext;
        this.seed = seed;
        this.snapshotInterval = snapshotInterval;

        this.globalStats = new FuzzerStatistics();
        this.blockGenerator = new BlockGenerator(rootContext, seed);
        this.lastSnapShotTime = 0L;
        this.snapshots = new LinkedList<>();
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
        lastSnapShotTime = System.currentTimeMillis();
        globalStats.start();
    }

    protected Long getStartTime() {
        return globalStats.getStartTime();
    }

    protected boolean exceededTimeBudget() {
        return System.currentTimeMillis() - getStartTime() >= getTimeBudgetMilis();
    }

    protected boolean shouldTakeSnapshot() {
        return System.currentTimeMillis() - lastSnapShotTime >= snapshotInterval;
    }

    protected void updateSnapshotTime() {
        lastSnapShotTime = System.currentTimeMillis();
    }

    void processSnapshot() {
        if (!shouldTakeSnapshot()) {
            return;
        }

        snapshots.add(takeSnapshot());
        updateSnapshotTime();
    }

    abstract List<CodeBlock> takeSnapshot();

    public List<List<CodeBlock>> getSnapshots() {
        return snapshots;
    }
}
