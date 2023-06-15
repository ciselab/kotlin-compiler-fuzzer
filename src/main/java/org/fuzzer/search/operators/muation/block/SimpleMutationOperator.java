package org.fuzzer.search.operators.muation.block;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.utils.RandomNumberGenerator;

public class SimpleMutationOperator extends ContextSensitiveMutationOperator {

    private final IndependentInsertionOperator independentInsertionOperator;

    private final RemovalOperator removalOperator;

    private final ContextualInsertionOperator contextualInsertionOperator;

    public SimpleMutationOperator(SyntaxNode nodeToSample, Context context, RandomNumberGenerator rng) {
        super(nodeToSample, context, rng);

        this.independentInsertionOperator = new IndependentInsertionOperator(nodeToSample, context, rng);
        this.removalOperator = new RemovalOperator(nodeToSample, context, rng);
        this.contextualInsertionOperator = new ContextualInsertionOperator(nodeToSample, context, rng);
    }

    @Override
    public CodeBlock mutate(CodeBlock block) {
        if (rng.fromUniformContinuous(0.0, 1.0) < 0.33) {
            return independentInsertionOperator.mutate(block);
        }

        if (rng.fromUniformContinuous(0.0, 1.0) < 0.33) {
            return removalOperator.mutate(block);
        }

        return contextualInsertionOperator.mutate(block);
    }
}
