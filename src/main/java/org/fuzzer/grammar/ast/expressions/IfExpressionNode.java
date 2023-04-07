package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IfExpressionNode extends ExpressionNode {
    public IfExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        KType sampledType = ctx.getRandomSamplableType();
        return getSampleOfType(rng, ctx, sampledType, true, generatedCallableDependencies).first();
    }

    @Override
    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type,
                                                                          boolean allowSubtypes, Set<String> generatedCallableDependencies) {
        KType booleanType = ctx.getTypeByName("Boolean");

        CodeFragment conditionCode = super.getSampleOfType(rng, ctx, booleanType, true, generatedCallableDependencies).first();
        CodeFragment trueBranchCode = new CodeFragment();
        CodeFragment falseBranchCode = new CodeFragment();

        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth);
        stmtNode.recordStatistics(stats);
        stmtNode.useConfiguration(cfg);

        // Sample some statements in the true branch
        int numberOfStatements = rng.fromGeometric();

        Context trueBranchContext = ctx.clone();
        Context falseBranchContext = ctx.clone();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, trueBranchContext, generatedCallableDependencies);
            trueBranchCode.extend(sampleExpr);
        }

        // Get a sound return type
        var trueCodeAndTypeParams = super.getSampleOfType(rng, trueBranchContext, type, true, generatedCallableDependencies);
        trueBranchCode.extend(trueCodeAndTypeParams.first());

        // Sample some statements in the false branch
        numberOfStatements = rng.fromGeometric();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, falseBranchContext, generatedCallableDependencies);
            falseBranchCode.extend(sampleExpr);
        }

        // At the moment, we ensure that the true and false branches return the exact same type
        // Such that the path of parameterized types is consistent
        // TODO generalize this
        KType returnType = trueCodeAndTypeParams.second().first();
        List<KType> parameterList = trueCodeAndTypeParams.second().second();

        // Get a sound return type for the false branch
        falseBranchCode.extend(super.getSampleOfType(rng, falseBranchContext, returnType, true, generatedCallableDependencies).first());

        CodeFragment code = new CodeFragment();

        code.appendToText("if (");
        code.appendToText(conditionCode);
        code.appendToText(") { ");
        code.extend(trueBranchCode);
        code.extend("} else {");
        code.extend(falseBranchCode);
        code.extend("}");

        if (this.stats != null) {
            stats.increment(SampleStructure.IF_EXPR);
        }

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}