package org.fuzzer.search.operators.muation.block;

import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;

public interface MutationOperator {
    
    CodeBlock mutate(CodeBlock block);
}
