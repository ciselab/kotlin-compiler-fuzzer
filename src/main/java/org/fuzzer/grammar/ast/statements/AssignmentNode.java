package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.expressions.SimpleExpressionNode;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.Set;

public class AssignmentNode extends StatementNode {

    private final int maxDepth;
    public AssignmentNode(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, maxDepth, stats, cfg);
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        KType type;
        String id;
        boolean sampleExisting = ctx.hasAssignableIdentifiers() && rng.randomBoolean();

        if (sampleExisting) {
            id = ctx.randomAssignableIdentifier();
            type = ctx.typeOfIdentifier(id);
        } else {
            id = ctx.getNewIdentifier();
            type = ctx.getRandomAssignableType();
        }

        SimpleExpressionNode expr = new SimpleExpressionNode(antlrNode, maxDepth, stats, cfg);

        var codeAndInstances = expr.getRandomExpressionNode(rng).getSampleOfType(rng, ctx, type, true, generatedCallableDependencies);

        String rhs = codeAndInstances.first().getText();
        String lhs = sampleExisting ? id : ("var " + id + ": " + ((KClassifierType) type).codeRepresentation(codeAndInstances.second().second()));

        if (!sampleExisting) {
            KType parameterizedTypeOfIdentifier = ((KClassifierType) type).withNewGenericInstances(codeAndInstances.second().second());
            ctx.addIdentifier(id, new KIdentifierCallable(id, parameterizedTypeOfIdentifier));
        }

        if (this.stats != null) {
            stats.increment(SampleStructure.ASSIGNMENT);
        }

        return new CodeFragment(lhs + " = " + rhs);
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Parameter node with children.");
        }
    }
}
