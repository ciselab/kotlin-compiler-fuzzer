package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.Set;

public class SimpleExpressionNode extends ExpressionNode {

    public SimpleExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        KType sampledType = ctx.getRandomSamplableType();

        if (this.stats != null) {
            stats.increment(SampleStructure.SIMPLE_EXPR);
        }

        return getSampleOfType(rng, ctx, sampledType, true, generatedCallableDependencies).first();
    }
}
