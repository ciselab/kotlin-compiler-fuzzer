package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.search.chromosome.FragmentType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.Set;

public class DoWhileNode extends StatementNode {

    public DoWhileNode(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, maxDepth, stats, cfg);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType boolType = ctx.getTypeByName("Boolean");
        CodeFragment code = CodeFragment
                .emptyFragmentOfType(FragmentType.STMT)
                .append("do {");

        ExpressionNode conditionNode = new ExpressionNode(antlrNode, maxDepth, stats, cfg);

        CodeFragment conditionCode = conditionNode.getRandomExpressionNode(rng).getSampleOfType(rng, ctx, boolType, true).first();

        int numberOfStatements = rng.fromDiscreteDistribution(cfg.getDoWhileDist());
        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth, stats, cfg)
                .getRandomStatementNode(rng);

        Context innerContext = ctx.clone();

        for (int statement = 0; statement < numberOfStatements; statement++) {
            code = code.extend(stmtNode.getSample(rng, innerContext));
        }

        code.extend("} while(")
                .extend(conditionCode)
                .extend(")");

        // Record this sample
        if (this.stats != null) {
            stats.increment(SampleStructure.DO_WHILE);
        }

        return code;
    }

    @Override
    public void invariant() {

    }
}
