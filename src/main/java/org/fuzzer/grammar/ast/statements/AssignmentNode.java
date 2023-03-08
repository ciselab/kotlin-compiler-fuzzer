package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.expressions.SimpleExpressionNode;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

public class AssignmentNode extends StatementNode {

    private final int maxDepth;
    public AssignmentNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
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

        SimpleExpressionNode expr = new SimpleExpressionNode(this.antlrNode, this.maxDepth);
        var codeAndInstances = expr.getRandomExpressionNode(rng).getSampleOfType(rng, ctx, type, true);

        String rhs = codeAndInstances.first().getText();
        String lhs = sampleExisting ? id : ("var " + id + ": " + ((KClassifierType) type).codeRepresentation(codeAndInstances.second().second()));

        if (!sampleExisting) {
            KType parameterizedTypeOfIdentifier = ((KClassifierType) type).withNewGenericInstances(codeAndInstances.second().second());
            ctx.addIdentifier(id, new KIdentifierCallable(id, parameterizedTypeOfIdentifier));
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
