package org.fuzzer.search.operators.muation.block;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public class IndependentInsertionOperator extends ContextSensitiveMutationOperator {

    protected IndependentInsertionOperator(SyntaxNode nodeToSample, Context context, RandomNumberGenerator rng) {
        super(nodeToSample, context, rng);
    }

    @Override
    public CodeBlock mutate(CodeBlock block) {
        List<CodeSnippet> currentSnippets = block.getSnippets();
        currentSnippets.addAll(getNewIndependentBlock().getSnippets());

        return new CodeBlock(currentSnippets);
    }
}
