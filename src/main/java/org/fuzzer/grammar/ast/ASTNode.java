package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public abstract class ASTNode {

    protected GrammarAST antlrNode;

    protected ASTNode parent;

    protected List<ASTNode> children;

    public ASTNode(GrammarAST antlrNode,
                   ASTNode parent,
                   List<ASTNode> children) {
        this.antlrNode = antlrNode;
        this.parent = parent;
        this.children = children;
    }

    public ASTNode(GrammarAST antlrNode, List<ASTNode> children) {
        this(antlrNode, null, children);
    }

    public abstract CodeFragment getSample(RandomNumberGenerator rng, Context ctx);

    public abstract void invariant();

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getParent() {
        return parent;
    }
}
