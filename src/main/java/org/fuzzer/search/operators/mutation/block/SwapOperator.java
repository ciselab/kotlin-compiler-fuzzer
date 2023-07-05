package org.fuzzer.search.operators.mutation.block;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.utils.RandomNumberGenerator;

public class SwapOperator extends ContextSensitiveMutationOperator {
    protected SwapOperator(SyntaxNode nodeToSample, Context context, RandomNumberGenerator rng) {
        super(nodeToSample, context, rng);
    }

    @Override
    public CodeBlock mutate(CodeBlock block) {
        return null;
    }
}
