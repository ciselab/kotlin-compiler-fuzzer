package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.FragmentType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.Set;

public class SimpleStatementNode extends StatementNode {
    public SimpleStatementNode(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, maxDepth, stats, cfg);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        if (this.stats != null) {
            stats.increment(SampleStructure.SIMPLE_STMT);
        }

        ExpressionNode expressionNode = new ExpressionNode(antlrNode, maxDepth, stats, cfg);

        return expressionNode.getSample(rng, ctx).withNewType(FragmentType.STMT);
    }
}
