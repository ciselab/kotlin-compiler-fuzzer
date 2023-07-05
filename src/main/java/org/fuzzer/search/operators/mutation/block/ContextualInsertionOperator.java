package org.fuzzer.search.operators.mutation.block;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public class ContextualInsertionOperator extends ContextSensitiveMutationOperator {

    protected ContextualInsertionOperator(SyntaxNode nodeToSample, Context context, RandomNumberGenerator rng) {
        super(nodeToSample, context, rng);
    }

    @Override
    public CodeBlock mutate(CodeBlock block) {
        List<CodeSnippet> currentSnippets = block.getSnippets();
        currentSnippets.addAll(getNewContextualBlock(block).getSnippets());

        return new CodeBlock(currentSnippets);
    }
}
