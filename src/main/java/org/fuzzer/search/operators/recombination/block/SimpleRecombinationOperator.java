package org.fuzzer.search.operators.recombination.block;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.search.chromosome.CodeBlock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleRecombinationOperator implements RecombinationOperator {

    @Override
    public CodeBlock combine(CodeBlock b1, CodeBlock b2) {
//        FuzzerStatistics combiedStatistics = FuzzerStatistics.aggregate(List.of(b1.stats(), b2.stats()));
//        combiedStatistics.stop();
//
//        CodeFragment combinedText = new CodeFragment(text, fragmentType);
//
//        combinedText.append(b1.text());
//        combinedText.extend(b2.text());
//
//        Set<KCallable> combinedDependencies = new HashSet<>();
//        combinedDependencies.addAll(b1.getCallables());
//        combinedDependencies.addAll(b2.getCallables());
//
//        return new CodeBlock(combiedStatistics, combinedText, combinedDependencies);

        return null;
    }
}
