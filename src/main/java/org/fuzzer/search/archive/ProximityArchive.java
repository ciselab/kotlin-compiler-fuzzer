package org.fuzzer.search.archive;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityArchive {
    private final Map<Integer, CodeBlock> archive;

    private final Map<Integer, Double> scores;


    public ProximityArchive(double[][] targets) {
        this.archive = new HashMap<>();
        this.scores = new HashMap<>();

        for (int i = 0; i < targets.length; i++) {
            this.scores.put(i, Double.MAX_VALUE);
        }
    }

    public void processEntry(CodeBlock codeBlock, double[] distances) {
        for (int i = 0; i < distances.length; i++) {
            double distance = distances[i];
            double currentScore = scores.get(i);

            if (distance < currentScore) {
                archive.put(i, codeBlock);
                scores.put(i, distance);
            }
        }
    }

    public Set<CodeBlock> getArchive() {
        return new HashSet<>(archive.values());
    }


}
