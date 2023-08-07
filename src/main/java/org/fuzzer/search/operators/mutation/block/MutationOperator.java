package org.fuzzer.search.operators.mutation.block;

import org.fuzzer.search.chromosome.CodeBlock;

public interface MutationOperator {
    
    CodeBlock mutate(CodeBlock block);
}
