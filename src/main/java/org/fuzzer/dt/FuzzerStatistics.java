package org.fuzzer.dt;

import org.fuzzer.grammar.SampleStructure;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class FuzzerStatistics implements Cloneable {
    private Map<SampleStructure, Long> extendedGrammarVisitations;

    private Long startTime;

    private Long finishTime;

    public FuzzerStatistics() {
        extendedGrammarVisitations = new HashMap<>();
        startTime = System.currentTimeMillis();
    }

    public FuzzerStatistics(Map<SampleStructure, Long> extendedGrammarVisitations, Long startTime) {
        this.extendedGrammarVisitations = extendedGrammarVisitations;
        this.startTime = startTime;
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        this.finishTime = System.currentTimeMillis();
    }

    public void increment(SampleStructure sampledStruct) {
        extendedGrammarVisitations.put(sampledStruct, extendedGrammarVisitations.getOrDefault(sampledStruct, 0L) + 1);
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

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String csv() {
        StringBuilder statsData = new StringBuilder(String.valueOf((finishTime - startTime)));
        for (SampleStructure s : SampleStructure.values()) {
            statsData.append(extendedGrammarVisitations.getOrDefault(s, 0L)).append(",");
        }

        return statsData.toString();
    }

    public static FuzzerStatistics aggregate(List<FuzzerStatistics> statList) {
        Map<SampleStructure, Long> cummalativeVisitations = new HashMap<>();
        for (SampleStructure struct : SampleStructure.values()) {
            cummalativeVisitations.put(struct, statList.stream()
                    .map(stats -> stats.extendedGrammarVisitations.getOrDefault(struct, 0L))
                    .reduce(0L, Long::sum));
        }

        return new FuzzerStatistics(cummalativeVisitations, statList.get(0).startTime);
    }

    public Long getNumberOfSamples(SampleStructure s) {
        return extendedGrammarVisitations.getOrDefault(s, 0L);
    }
}
