package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.search.chromosome.FragmentType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.List;
import java.util.Set;

public class IfExpressionNode extends ExpressionNode {
    public IfExpressionNode(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, maxDepth, stats, cfg);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType sampledType = ctx.getRandomSamplableType();
        return getSampleOfType(rng, ctx, sampledType, true).first();
    }

    @Override
    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type,
                                                                          boolean allowSubtypes) {
        KType booleanType = ctx.getTypeByName("Boolean");

        CodeFragment conditionCode = super.getSampleOfType(rng, ctx, booleanType, true).first();
        CodeFragment trueBranchCode = CodeFragment.emptyFragmentOfType(FragmentType.EXPR);
        CodeFragment falseBranchCode = CodeFragment.emptyFragmentOfType(FragmentType.EXPR);

        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth, stats, cfg);
        // Sample some statements in the true branch
        int numberOfStatements = rng.fromDiscreteDistribution(cfg.getIfDist());

        Context trueBranchContext = ctx.clone();
        Context falseBranchContext = ctx.clone();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, trueBranchContext);
            trueBranchCode = trueBranchCode.extend(sampleExpr);
        }

        // Get a sound return type
        var trueCodeAndTypeParams = super.getSampleOfType(rng, trueBranchContext, type, true);
        trueBranchCode = trueBranchCode.extend(trueCodeAndTypeParams.first());

        // Sample some statements in the false branch
        numberOfStatements = rng.fromDiscreteDistribution(cfg.getElseDist());

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, falseBranchContext);
            falseBranchCode = falseBranchCode.extend(sampleExpr);
        }

        // At the moment, we ensure that the true and false branches return the exact same type
        // Such that the path of parameterized types is consistent
        // TODO generalize this
        KType returnType = trueCodeAndTypeParams.second().first();
        List<KType> parameterList = trueCodeAndTypeParams.second().second();

        // Get a sound return type for the false branch
        falseBranchCode = falseBranchCode.extend(super.getSampleOfType(rng, falseBranchContext, returnType, true).first());

        CodeFragment code = CodeFragment.emptyFragmentOfType(FragmentType.EXPR);

        code.append("if (")
                .append(conditionCode)
                .append(") { ")
                .extend(trueBranchCode)
                .extend("} else {")
                .extend(falseBranchCode)
                .extend("}");

        if (this.stats != null) {
            stats.increment(SampleStructure.IF_EXPR);
        }

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}