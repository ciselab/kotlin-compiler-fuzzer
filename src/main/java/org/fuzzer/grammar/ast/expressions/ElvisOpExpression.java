package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.search.chromosome.FragmentType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElvisOpExpression extends ExpressionNode {
    public ElvisOpExpression(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
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
        // Get a sound return type
        var lhsCodeAndParams = super.getSampleOfType(rng, ctx, type, true);

        // At the moment, we ensure that the true and false branches return the exact same type
        // Such that the path of parameterized types is consistent
        // TODO generalize this
        KType returnType = lhsCodeAndParams.second().first();
        List<KType> parameterList = lhsCodeAndParams.second().second();

        var rhsCodeAndParams = super.getSampleOfType(rng, ctx, returnType, true);

        CodeFragment code = new CodeFragment(
                List.of(lhsCodeAndParams.first(),
                        CodeFragment.textCodeFragment(" ?: "),
                        rhsCodeAndParams.first()),
                FragmentType.EXPR);

        if (stats != null) {
            stats.increment(SampleStructure.ELVIS_OP);
        }

        return new Tuple<>(code, new Tuple<>(returnType, parameterList));
    }
}
