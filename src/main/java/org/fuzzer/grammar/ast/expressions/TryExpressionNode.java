package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.List;

public class TryExpressionNode extends ExpressionNode {
    public TryExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType sampledType = ctx.getRandomSamplableType();
        return getSampleOfType(rng, ctx, sampledType, true).first();
    }

    @Override
    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type, boolean allowSubtypes) {
        KType throwableType = ctx.getTypeByName("Throwable");

        CodeFragment tryCode = new CodeFragment();
        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth);

        // Sample some statements in the try block
        int numberOfStatements = rng.fromGeometric();

        Context tryContext = ctx.clone();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, tryContext);
            tryCode.extend(sampleExpr);
        }

        // Get a sound return type
        var tryCodeAndTypeParams = super.getSampleOfType(rng, tryContext, type, true);
        tryCode.extend(tryCodeAndTypeParams.first());

        CodeFragment code = new CodeFragment();
        code.appendToText("try {");
        code.extend(tryCode);
        code.extend("}");

        // At the moment, we ensure that the true and false branches return the exact same type
        // Such that the path of parameterized types is consistent
        // TODO generalize this
        KType returnType = tryCodeAndTypeParams.second().first();
        List<KType> parameterList = tryCodeAndTypeParams.second().second();

        int numberOfCatchBlocks = rng.fromGeometric();

        // Sample some catch blocks
        for (int i = 0; i < numberOfCatchBlocks; i ++) {
            Context catchContext = ctx.clone();
            KType exceptionToCatch  = ctx.randomSubtypeOf(throwableType);
            CodeFragment catchCode = new CodeFragment("catch (" + ctx.getNewIdentifier() + ": " + exceptionToCatch.name() + ") {");

            // Sample some statements in the catch block
            numberOfStatements = rng.fromGeometric();

            for (int j = 0; j < numberOfStatements; j++) {
                CodeFragment sampleExpr = stmtNode.getSample(rng, catchContext);
                catchCode.extend(sampleExpr);
            }

            // Get a sound return type for the false branch
            catchCode.extend(super.getSampleOfType(rng, catchContext, returnType, true).first());
            catchCode.extend("}");
            code.extend(catchCode);

        }

        // Sample a finally block
        if (rng.randomBoolean() || numberOfCatchBlocks == 0) {
            Context finallyContext = ctx.clone();
            CodeFragment finallyCode = new CodeFragment("finally {");

            numberOfStatements = rng.fromGeometric();

            for (int j = 0; j < numberOfStatements; j++) {
                CodeFragment sampleExpr = stmtNode.getSample(rng, finallyContext);
                finallyCode.extend(sampleExpr);
            }

            finallyCode.extend("}");
            code.extend(finallyCode);
        }

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}
