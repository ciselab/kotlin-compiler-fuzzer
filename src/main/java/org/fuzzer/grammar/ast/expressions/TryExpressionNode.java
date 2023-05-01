package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.List;
import java.util.Set;

public class TryExpressionNode extends ExpressionNode {
    public TryExpressionNode(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, maxDepth, stats, cfg);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        KType sampledType = ctx.getRandomSamplableType();
        return getSampleOfType(rng, ctx, sampledType, true, generatedCallableDependencies).first();
    }

    @Override
    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type,
                                                                          boolean allowSubtypes, Set<String> generatedCallableDependencies) {

        KType throwableType = ctx.getTypeByName("Throwable");

        CodeFragment tryCode = new CodeFragment();
        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth, stats, cfg);

        // Sample some statements in the try block
        int numberOfStatements = rng.fromDiscreteDistribution(cfg.getTryDist());

        Context tryContext = ctx.clone();

        for (int i = 0; i < numberOfStatements; i++) {
            CodeFragment sampleExpr = stmtNode.getSample(rng, tryContext, generatedCallableDependencies);
            tryCode.extend(sampleExpr);
        }

        // Get a sound return type
        var tryCodeAndTypeParams = super.getSampleOfType(rng, tryContext, type, true, generatedCallableDependencies);
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

        int numberOfCatchBlocks = rng.fromDiscreteDistribution(cfg.getCatchNumberDist());

        // Sample some catch blocks
        for (int i = 0; i < numberOfCatchBlocks; i ++) {
            Context catchContext = ctx.clone();
            KType exceptionToCatch  = ctx.randomSubtypeOf(throwableType);
            CodeFragment catchCode = new CodeFragment("catch (" + ctx.getNewIdentifier() + ": " + exceptionToCatch.name() + ") {");

            // Sample some statements in the catch block
            numberOfStatements = rng.fromDiscreteDistribution(cfg.getCatchStmtDist());

            for (int j = 0; j < numberOfStatements; j++) {
                CodeFragment sampleExpr = stmtNode.getSample(rng, catchContext, generatedCallableDependencies);
                catchCode.extend(sampleExpr);
            }

            // Get a sound return type for the false branch
            catchCode.extend(super.getSampleOfType(rng, catchContext, returnType, true, generatedCallableDependencies).first());
            catchCode.extend("}");
            code.extend(catchCode);

        }

        // Sample a finally block
        if (numberOfCatchBlocks == 0 || rng.randomBoolean(cfg.getFinallyProbability())) {
            Context finallyContext = ctx.clone();
            CodeFragment finallyCode = new CodeFragment("finally {");

            numberOfStatements = rng.fromDiscreteDistribution(cfg.getFinallyDist());

            for (int j = 0; j < numberOfStatements; j++) {
                CodeFragment sampleExpr = stmtNode.getSample(rng, finallyContext, generatedCallableDependencies);
                finallyCode.extend(sampleExpr);
            }

            finallyCode.extend("}");
            code.extend(finallyCode);
        }

        if (this.stats != null) {
            stats.increment(SampleStructure.TRY_CATCH);
        }

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}
