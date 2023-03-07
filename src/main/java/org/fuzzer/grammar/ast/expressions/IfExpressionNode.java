package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.List;

public class IfExpressionNode extends ExpressionNode {
    public IfExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType sampledType = ctx.getRandomSamplableType();
        return getSampleOfType(rng, ctx, sampledType, true).first();
    }

    @Override
    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type, boolean allowSubtypes) {
        KType booleanType = ctx.getTypeByName("Boolean");

        CodeFragment conditionCode = getSampleOfType(rng, ctx, booleanType, true).first();
        CodeFragment trueBranchCode = new CodeFragment();
        CodeFragment falseBranchCode = new CodeFragment();

        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth);

        // Sample some statements in the true branch
        int numberOfStatements = rng.fromGeometric();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, ctx);
            trueBranchCode.extend(sampleExpr);
        }
        // Get a sound return type
        var trueCodeAndTypeParams = super.getSampleOfType(rng, ctx, type, true);
        trueBranchCode.extend(trueCodeAndTypeParams.first());

        // Sample some statements in the false branch
        numberOfStatements = rng.fromGeometric();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, ctx);
            falseBranchCode.extend(sampleExpr);
        }

        // At the moment, we ensure that the true and false branches return the exact same type
        // Such that the path of parameterized types is consistent
        // TODO generalize this
        KType returnType = trueCodeAndTypeParams.second().first();
        List<KType> parameterList = trueCodeAndTypeParams.second().second();

        // Get a sound return type for the false branch
        falseBranchCode.extend(super.getSampleOfType(rng, ctx, returnType, true).first());

        CodeFragment code = new CodeFragment();

        code.appendToText("if (");
        code.appendToText(conditionCode);
        code.appendToText(") { ");
        code.extend(trueBranchCode);
        code.extend("} else {");
        code.extend(falseBranchCode);
        code.extend("}");

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}