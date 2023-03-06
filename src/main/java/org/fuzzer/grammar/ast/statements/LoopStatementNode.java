package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;

public class LoopStatementNode extends ASTNode {

    public LoopStatementNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        super(antlrNode, parent, children);
    }

    public LoopStatementNode(GrammarAST anltrNode, int maxDepth) {
        super(anltrNode, new LinkedList<>());
        List<ASTNode> children = new LinkedList<>();
        // TODO
        this.children = children;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return null;
    }

    @Override
    public void invariant() {

    }
}
