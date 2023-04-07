package org.fuzzer.search.operators.recombination;

import org.fuzzer.search.chromosome.CodeBlock;

public interface RecombinationOperator {
    public CodeBlock combine(CodeBlock b1, CodeBlock b2);
}
