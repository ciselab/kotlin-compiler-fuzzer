package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TextNode extends ASTNode {

    private final String text;

    public TextNode(GrammarAST antlrNode, String text, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, new LinkedList<>(), stats, cfg);
        this.text = text;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        invariant();

        return new CodeFragment(text);
    }

    @Override
    public void invariant() {
        if (children.isEmpty()) {
            throw new IllegalStateException("Text node with children.");
        }
    }
}
