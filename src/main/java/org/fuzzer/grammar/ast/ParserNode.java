package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.grammar.Constraint;

import java.util.List;

public abstract class ParserNode implements ASTNode {
    private ASTNodeType type;
    private GrammarAST antlrNode;

    private ParserNode parent;

    private List<ASTNode> children;

    public ParserNode(ASTNodeType type, GrammarAST antlrNode, ParserNode parent, List<ASTNode> children) {
        this.type = type;
        this.antlrNode = antlrNode;
        this.parent = parent;
        this.children = children;
    }

    public ASTNodeType getType() {
        return type;
    }

    @Override
    public ASTNode getParent() {
        return parent;
    }

    @Override
    public List<ASTNode> getChildren() {
        return children;
    }

    public abstract List<Constraint> getConstraints();

    @Override
    public boolean invariant() {
        return true;
    }
}
