package org.fuzzer.dt;

import org.fuzzer.grammar.SampleStructure;

import java.util.*;

public class FuzzerStatistics implements Cloneable {
    private Map<SampleStructure, Long> extendedGrammarVisitations;

    private Long startTime;

    private Long finishTime;

    public FuzzerStatistics() {
        extendedGrammarVisitations = new HashMap<>();
        startTime = System.currentTimeMillis();
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
}
