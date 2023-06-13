package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeConstruct;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Set;

public class AltNode extends ASTNode {

    public AltNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, parent, children, stats, cfg);
    }

    public AltNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, stats, cfg);
    }

    @Override
    public CodeConstruct getSample(RandomNumberGenerator rng, Context ctx) {
        invariant();

        ASTNode nodeToSample = rng.selectFromList(children);
        return nodeToSample.getSample(rng, ctx);
    }

    @Override
    public void invariant() {
        boolean holds = !children.isEmpty();

        if (!holds) {
            throw new IllegalStateException("Alt node with no alternatives encountered.");
        }
    }
}
