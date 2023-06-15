package org.fuzzer.search.operators.muation.block;

import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.HashSet;
import java.util.List;

public class RemovalOperator extends ContextSensitiveMutationOperator {

    protected RemovalOperator(SyntaxNode nodeToSample, Context context, RandomNumberGenerator rng) {
        super(nodeToSample, context, rng);
    }

    @Override
    public CodeBlock mutate(CodeBlock block) {
        CodeSnippet rootSnippet = rng.selectFromList(block.getSnippets());
        List<CodeSnippet> snippetsToRemove = block.getUpStreamDependents(rootSnippet);

        // If all blocks would be removed by the operator, skip mutation
        if (new HashSet<>(snippetsToRemove).containsAll(block.getSnippets())) {
            return block;
        }

        List<CodeSnippet> remainingSnippets = block
                .getSnippets().stream()
                .filter(snippet -> !snippetsToRemove.contains(snippet))
                .toList();

        return new CodeBlock(remainingSnippets);
    }
}
