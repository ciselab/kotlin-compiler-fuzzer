package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

public class SimpleStatementNode extends StatementNode {
    public SimpleStatementNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return new ExpressionNode(antlrNode, maxDepth).getSample(rng, ctx);
    }
}
