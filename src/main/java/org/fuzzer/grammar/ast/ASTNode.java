package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeConstruct;
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
                   List<ASTNode> children,
                   FuzzerStatistics stats,
                   Configuration cfg) {
        this.antlrNode = antlrNode;
        this.parent = parent;
        this.children = children;
        this.stats = stats;
        this.cfg = cfg;

        for (ASTNode child : children) {
            child.recordStatistics(stats);
            child.useConfiguration(cfg);
        }
    }

    public ASTNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        this(antlrNode, null, children, stats, cfg);
    }

    public abstract CodeConstruct getSample(RandomNumberGenerator rng, Context ctx);

    public void recordStatistics(FuzzerStatistics stats) {
        this.stats = stats;
        for (ASTNode child : children) {
            child.recordStatistics(stats);
        }
    }

    protected void useConfiguration(Configuration cfg) {
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
