package org.fuzzer.dt;

import org.fuzzer.grammar.SampleStructure;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class FuzzerStatistics implements Cloneable {
    private Map<SampleStructure, Long> extendedGrammarVisitations;

    private Long depth;

    private Long startTime;

    private Long finishTime;

    public FuzzerStatistics() {
        this.extendedGrammarVisitations = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        this.depth = 0L;
    }

    public FuzzerStatistics(Map<SampleStructure, Long> extendedGrammarVisitations, Long startTime, Long depth) {
        this.extendedGrammarVisitations = extendedGrammarVisitations;
        this.startTime = startTime;
        this.depth = depth;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getDepth() {
        return depth;
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.finishTime = System.currentTimeMillis();
    }

    public void incrementDepth() {
        this.depth++;
    }

    public void increment(SampleStructure sampledStruct) {
        incrementBy(sampledStruct, 1L);
    }

    public void incrementBy(SampleStructure sampledStruct, Long value) {
        extendedGrammarVisitations.put(sampledStruct, extendedGrammarVisitations.getOrDefault(sampledStruct, 0L) + value);
    }

    public void reset() {
        extendedGrammarVisitations.clear();
        startTime = System.currentTimeMillis();
    }

    public void resetVisitations() {
        extendedGrammarVisitations.clear();
    }

    @Override
    public FuzzerStatistics clone() {
        try {
            FuzzerStatistics clone = (FuzzerStatistics) super.clone();
            clone.extendedGrammarVisitations = new HashMap<>(extendedGrammarVisitations);
            clone.startTime = startTime;
            clone.finishTime = finishTime;
            clone.depth = depth;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String csv() {
        StringBuilder statsData = new StringBuilder(String.valueOf((finishTime - startTime)));
        statsData.append(",");
        for (SampleStructure s : SampleStructure.values()) {
            statsData.append(extendedGrammarVisitations.getOrDefault(s, 0L)).append(",");
        }

        return statsData.toString();
    }

    public static FuzzerStatistics aggregate(List<FuzzerStatistics> statList) {
        Map<SampleStructure, Long> cummalativeVisitations = new HashMap<>();
        Long totalDepth = 0L;

        for (FuzzerStatistics stats : statList) {
            totalDepth += stats.depth;
        }

        for (SampleStructure struct : SampleStructure.values()) {
            cummalativeVisitations.put(struct, statList.stream()
                    .map(stats -> stats.extendedGrammarVisitations.getOrDefault(struct, 0L))
                    .reduce(0L, Long::sum));
        }

        return new FuzzerStatistics(cummalativeVisitations, statList.get(0).startTime, totalDepth);
    }

    public double[] getVisitations() {
        double[] visitations = new double[SampleStructure.values().length];
        for (int i = 0; i < SampleStructure.values().length; i++) {
            visitations[i] = extendedGrammarVisitations.getOrDefault(SampleStructure.values()[i], 0L);
        }

        return visitations;
    }

    public Long getNumberOfSamples(SampleStructure s) {
        return extendedGrammarVisitations.getOrDefault(s, 0L);
    }
}
