package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.SimpleExpressionNode;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.ArrayList;
import java.util.List;

public class AssignmentNode extends ASTNode {

    private final int maxDepth;
    public AssignmentNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new ArrayList<>());
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType type;
        String id;
        boolean sampleExisting = ctx.hasAnyVariables() && rng.randomBoolean();

        if (sampleExisting) {
            id = ctx.randomIdentifier();
            type = ctx.typeOfIdentifier(id);
        } else {
            id = ctx.getNewIdentifier();
            type = ctx.getRandomSamplableType();
        }

        SimpleExpressionNode expr = new SimpleExpressionNode(this.antlrNode, this.maxDepth);
        var codeAndInstances = expr.getSampleOfType(rng, ctx, type, true);

        String rhs = codeAndInstances.first().getText();
        String lhs = sampleExisting ? id : ("var " + id + ": " + ((KClassifierType) type).codeRepresentation(codeAndInstances.second().second()));

        if (!sampleExisting) {
            ctx.addIdentifier(id, new KIdentifierCallable(id, type));
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
