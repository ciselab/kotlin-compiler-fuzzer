package org.fuzzer.search.operators.recombination.block;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.utils.Tuple;

public interface RecombinationOperator {
    public Tuple<CodeBlock, CodeBlock> combine(CodeBlock b1, CodeBlock b2);
}
