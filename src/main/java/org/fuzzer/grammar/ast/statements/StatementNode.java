package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;

public class StatementNode extends ASTNode {

    public StatementNode(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    public StatementNode(GrammarAST anltrNode, int maxDepth) {
        super(anltrNode, new LinkedList<>());
        List<ASTNode> children = new LinkedList<>();
        children.add(new AssignmentNode(anltrNode, maxDepth));
        children.add(new ExpressionNode(antlrNode, maxDepth));
        this.children = children;
    }
    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        ASTNode nodeToSample = children.get(rng.fromUniformDiscrete(0, children.size() - 1));
        return nodeToSample.getSample(rng, ctx.clone());
    }

    @Override
    public void invariant() {

    }
}
