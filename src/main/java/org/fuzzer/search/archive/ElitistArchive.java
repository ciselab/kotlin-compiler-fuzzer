package org.fuzzer.search.archive;

import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.syntax.BlockNode;
import org.fuzzer.representations.chromosome.CodeBlock;
import org.fuzzer.utils.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElitistArchive {
    private final Set<CodeBlock> archive;

    // Pretty terrible way to show that only the LOC should be minimized
    private final static List<Boolean> minimize = List.of(true, false, false, false, false, false, false, false, false, false, false, false, false);

    public ElitistArchive() {
        archive = new HashSet<>();
    }

    public static int dominates(CodeBlock b1, CodeBlock b2) {
        int b1Superior = 0, b2Superior = 0, bothEqual = 0;

        int featureIndex = 0;
        for (SampleStructure feature : SampleStructure.values()) {
            long b1Feature = b1.getNumberOfSamples(feature);
            long b2Feature = b2.getNumberOfSamples(feature);

            boolean shouldMinimize = minimize.get(featureIndex);
            Integer comparisonResult = Long.compare(b1Feature, b2Feature);

            if (shouldMinimize) {
                comparisonResult *= -1;
            }

            switch (comparisonResult) {
                case 1 -> {
                    b1Superior++;
                }
                case -1 -> {
                    b2Superior++;
                }
                default -> {
                    bothEqual++;
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

    public boolean add(CodeBlock b) {
        Set<CodeBlock> dominated = new HashSet<>();
        boolean isDominated = false;

        for (CodeBlock b1 : archive) {
            int comparisonResult = dominates(b, b1);

            if (comparisonResult > 1) {
                dominated.add(b1);
                continue;
            }

            if (comparisonResult < 1) {
                isDominated = true;
                // TODO uncomment. Only commented as to improve the power of the following check.
//                break;
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
}
