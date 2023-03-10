package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.List;

public class ElvisOpExpression extends ExpressionNode {
    public ElvisOpExpression(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType sampledType = ctx.getRandomSamplableType();
        return getSampleOfType(rng, ctx, sampledType, true).first();
    }

    @Override
    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type, boolean allowSubtypes) {        // Get a sound return type
        var lhsCodeAndParams = super.getSampleOfType(rng, ctx, type, true);

        // At the moment, we ensure that the true and false branches return the exact same type
        // Such that the path of parameterized types is consistent
        // TODO generalize this
        KType returnType = lhsCodeAndParams.second().first();
        List<KType> parameterList = lhsCodeAndParams.second().second();

        var rhsCodeAndParams = super.getSampleOfType(rng, ctx, returnType, true);

        CodeFragment code = new CodeFragment();
        code.appendToText(lhsCodeAndParams.first());
        code.appendToText(" ?: ");
        code.appendToText(rhsCodeAndParams.first());

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}
