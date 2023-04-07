package org.fuzzer.search.operators.recombination;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.search.chromosome.CodeBlock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleRecombinationOperator implements RecombinationOperator {

    @Override
    public CodeBlock combine(CodeBlock b1, CodeBlock b2) {
        FuzzerStatistics combiedStatistics = FuzzerStatistics.aggregate(List.of(b1.getStats(), b2.getStats()));
        combiedStatistics.stop();

        CodeFragment combinedText = new CodeFragment();

        combinedText.appendToText(b1.getText());
        combinedText.extend(b2.getText());

        Set<KCallable> combinedDependencies = new HashSet<>();
        combinedDependencies.addAll(b1.getCallables());
        combinedDependencies.addAll(b2.getCallables());

        return new CodeBlock(combiedStatistics, combinedText, combinedDependencies);
    }
}
