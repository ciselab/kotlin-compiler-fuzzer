package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;

import java.util.List;

public class AssignmentNode extends ParserNode {

    public AssignmentNode(ASTNodeType type, GrammarAST antlrNode, ParserNode parent, List<ASTNode> children) {
        super(type, antlrNode, parent, children);
    }

    @Override
    public CodeFragment getSample(Context ctx) {
        return null;
    }

    @Override
    public boolean invariant() {
        return false;
    }

    @Override
    public List<ASTNode> getChildren() {
        return null;
    }
}
