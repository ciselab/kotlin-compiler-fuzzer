package org.fuzzer.search.operators.muation.block;

import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.utils.RandomNumberGenerator;

public abstract class ContextSensitiveMutationOperator implements MutationOperator {

    private final Context context;

    protected final RandomNumberGenerator rng;

    private final ASTNode nodeToSample;


    protected ContextSensitiveMutationOperator(SyntaxNode nodeToSample, Context context, RandomNumberGenerator rng) {
        this.nodeToSample = nodeToSample;
        this.context = context;
        this.rng = rng;
    }

    protected CodeBlock getNewIndependentBlock() {
        return (CodeBlock) nodeToSample.getSample(rng, context.clone());
    }

    protected CodeBlock getNewContextualBlock(CodeBlock block) {
        Context blockContext = context.clone();
        block.buildContext(blockContext);

        return (CodeBlock) nodeToSample.getSample(rng, blockContext);
    }
}
