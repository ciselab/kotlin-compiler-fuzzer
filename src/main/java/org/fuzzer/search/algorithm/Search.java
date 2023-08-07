package org.fuzzer.search.algorithm;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.operators.generator.BlockGenerator;
import org.fuzzer.utils.AsyncSnapshotWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    protected Long snapshotNumber;

    protected String outputDirectory;

    public Search(ASTNode nodeToSample, Long timeBudgetMilis,
                  Context rootContext, Long seed, Long snapshotInterval,
                  String outputDirectory) {
        this.nodeToSample = nodeToSample;
        this.timeBudgetMilis = timeBudgetMilis;
        this.rootContext = rootContext;
        this.seed = seed;
        this.snapshotInterval = snapshotInterval;

        this.globalStats = new FuzzerStatistics();
        this.blockGenerator = new BlockGenerator(rootContext, seed);
        this.lastSnapShotTime = 0L;
        this.snapshotNumber = 0L;
        this.outputDirectory = outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
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

    public abstract List<CodeBlock> search(boolean takeSnapshots);

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

        List<CodeBlock> snapshot = takeSnapshot();
        String snapshotDir = this.outputDirectory + "snapshot-" + snapshotNumber++;
        try {
            Files.createDirectory(Paths.get(snapshotDir));
            Files.createDirectory(Paths.get(snapshotDir + "/v1"));
            Files.createDirectory(Paths.get(snapshotDir + "/v2"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AsyncSnapshotWriter snapshotWriter = new AsyncSnapshotWriter(snapshotDir, snapshot);
        snapshotWriter.start();

        updateSnapshotTime();
    }

    abstract List<CodeBlock> takeSnapshot();
}
