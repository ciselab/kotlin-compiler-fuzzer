package org.fuzzer.search.operators.recombination.block;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.LinkedList;
import java.util.List;

public class SimpleRecombinationOperator implements RecombinationOperator {

    private final RandomNumberGenerator rng;

    public SimpleRecombinationOperator(RandomNumberGenerator rng) {
        this.rng = rng;
    }

    private Tuple<List<CodeSnippet>, List<CodeSnippet>> splitBlock(CodeBlock b) {
        CodeSnippet rootSnippet = rng.selectFromList(b.getSnippets());
        List<CodeSnippet> snippetsToRemove = b.getDependencyTopology(rootSnippet);

        List<CodeSnippet> snippetsToKeep = b.getSnippets().stream()
                .filter(snippet -> !snippetsToRemove.contains(snippet))
                .toList();

        return new Tuple<>(snippetsToKeep, snippetsToRemove);
    }


    @Override
    public Tuple<CodeBlock, CodeBlock> combine(CodeBlock b1, CodeBlock b2) {
        Tuple<List<CodeSnippet>, List<CodeSnippet>> split1 = splitBlock(b1);
        Tuple<List<CodeSnippet>, List<CodeSnippet>> split2 = splitBlock(b2);

        LinkedList<CodeSnippet> newSnippets1 = new LinkedList<>();
        newSnippets1.addAll(split1.first());
        newSnippets1.addAll(split2.second());

        LinkedList<CodeSnippet> newSnippets2 = new LinkedList<>();
        newSnippets2.addAll(split2.first());
        newSnippets2.addAll(split1.second());

        return new Tuple<>(new CodeBlock(newSnippets1), new CodeBlock(newSnippets2));
    }
}
