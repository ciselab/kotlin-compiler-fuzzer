package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.Set;

public class SimpleStatementNode extends StatementNode {
    public SimpleStatementNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        if (this.stats != null) {
            stats.increment(SampleStructure.SIMPLE_STMT);
        }

        ExpressionNode expressionNode = new ExpressionNode(antlrNode, maxDepth);
        expressionNode.recordStatistics(stats);
        expressionNode.useConfiguration(cfg);

        return expressionNode.getSample(rng, ctx, generatedCallableDependencies);
    }
}
