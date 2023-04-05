package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Set;

public abstract class ASTNode {

    protected GrammarAST antlrNode;

    protected ASTNode parent;

    protected List<ASTNode> children;

    protected FuzzerStatistics stats;

    protected Configuration cfg;

    public ASTNode(GrammarAST antlrNode,
                   ASTNode parent,
                   List<ASTNode> children) {
        this.antlrNode = antlrNode;
        this.parent = parent;
        this.children = children;
        this.stats = null;
        this.cfg = null;
    }

    public ASTNode(GrammarAST antlrNode, List<ASTNode> children) {
        this(antlrNode, null, children);
    }

    public abstract CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies);

    public void recordStatistics(FuzzerStatistics stats) {
        this.stats = stats;
        for (ASTNode child : children) {
            child.recordStatistics(stats);
        }
    }

    public void useConfiguration(Configuration cfg) {
        this.cfg = cfg;
        for (ASTNode child : children) {
            child.useConfiguration(cfg);
        }
    }

    public abstract void invariant();

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getParent() {
        return parent;
    }
}
