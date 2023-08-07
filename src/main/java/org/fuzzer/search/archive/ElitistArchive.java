package org.fuzzer.search.archive;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.MOFitnessFunction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElitistArchive {
    private final HashSet<CodeBlock> archive;

    // Pretty terrible way to show that only the LOC should be minimized
    private final boolean[] shouldMinimize;

    private final MOFitnessFunction fitnessFunction;

    public ElitistArchive(MOFitnessFunction fitnessFunction, boolean[] shouldMinimize) {
        this.archive = new HashSet<>();
        this.fitnessFunction = fitnessFunction;
        this.shouldMinimize = shouldMinimize;
    }

    public static int dominates(CodeBlock b1, CodeBlock b2,
                                MOFitnessFunction fitnessFunction, boolean[] shouldMinimize) {
        return dominates(fitnessFunction.evaluate(b1), fitnessFunction.evaluate(b2), shouldMinimize);
    }

    public static int dominates(double[] f1, double[] f2, boolean[] shouldMinimize) {
        int b1Superior = 0, b2Superior = 0;

        for (int i = 0; i < f1.length; i++) {
            double b1Feature = f1[i];
            double b2Feature = f2[i];

            boolean shouldMinimizeFeature = shouldMinimize[i];
            int comparisonResult = Double.compare(b1Feature, b2Feature);

            if (shouldMinimizeFeature) {
                comparisonResult *= -1;
            }

            switch (comparisonResult) {
                case 1 -> {
                    b1Superior++;
                }
                case -1 -> {
                    b2Superior++;
                }
            }
        }

        if (b1Superior > 0 && b2Superior == 0) {
            return 1;
        }

        if (b2Superior > 0 && b1Superior == 0) {
            return -1;
        }

        return 0;
    }

    public boolean add(CodeBlock b, MOFitnessFunction fitnessFunction) {
        if (archive.contains(b)) {
            return false;
        }

        Set<CodeBlock> dominated = new HashSet<>();
        boolean isDominated = false;

        for (CodeBlock b1 : archive) {
            int comparisonResult = dominates(b, b1, fitnessFunction, shouldMinimize);

            if (comparisonResult == 1) {
                dominated.add(b1);
                continue;
            }

            if (comparisonResult == -1) {
                isDominated = true;
                break;
            }
        }

        if (isDominated && !dominated.isEmpty()) {
            throw new IllegalStateException("Illegal archive state detected.");
        }

        if (!dominated.isEmpty()) {
            archive.removeAll(dominated);
        }

        if (!isDominated) {
            archive.add(b);
            return true;
        }

        return false;
    }

    public void addAll(List<CodeBlock> population, MOFitnessFunction fitnessFunction) {
        for (CodeBlock b : population) {
            add(b, fitnessFunction);
        }
    }

    public Set<CodeBlock> getArchive() {
        return archive;
    }
}
