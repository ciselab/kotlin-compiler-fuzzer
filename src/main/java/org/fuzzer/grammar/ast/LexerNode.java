package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;

import java.util.ArrayList;
import java.util.List;

public abstract class LexerNode implements ASTNode {
    private ASTNodeType type;
    private GrammarAST antlrNode;

    private ASTNode parent;

    private List<ASTNode> children;

    public LexerNode(ASTNodeType type, GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        this.type = type;
        this.antlrNode = antlrNode;
        this.parent = parent;
        this.children = children;
    }

    // For debugging purposes
    public LexerNode() {
        this.type = ASTNodeType.NOTHING;
        this.antlrNode = null;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    @Override
    public ASTNode getParent() {
        return parent;
    }

    @Override
    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public boolean invariant() {
        return true;
    }
}
