package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.StringUtilities;
import org.fuzzer.utils.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.fuzzer.utils.StringUtilities.removeGeneric;

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
            id = StringUtilities.randomIdentifier();
            while (ctx.containsIdentifier(id)) {
                id = StringUtilities.randomIdentifier();
            }
            type = ctx.getRandomType();

            // Hack for demonstration purposes
            while (removeGeneric(type.name()).equals("Comparable")) {
                type = ctx.getRandomType();
            }
        }

        ExpressionNode expr = new ExpressionNode(this.antlrNode, this.maxDepth);

        String rhs = expr.getSampleOfType(rng, ctx, type).first().getText();
        String lhs = sampleExisting ? id : ("var " + id + ": " + type.name());

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
