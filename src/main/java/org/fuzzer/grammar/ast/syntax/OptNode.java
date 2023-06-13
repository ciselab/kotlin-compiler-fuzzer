package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeConstruct;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Set;

public class OptNode extends ASTNode {

    public OptNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, parent, children, stats, cfg);
    }

    public OptNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, stats, cfg);
    }

    @Override
    public CodeConstruct getSample(RandomNumberGenerator rng, Context ctx) {
        if (rng.randomBoolean()) {
            return children.get(0).getSample(rng, ctx);
        } else {
            return new CodeSnippet();
        }
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Optional node with more than one child.");
        }
    }
}
