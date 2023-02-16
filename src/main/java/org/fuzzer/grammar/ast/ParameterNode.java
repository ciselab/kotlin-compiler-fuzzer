package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.StringUtilities;

import java.util.ArrayList;
import java.util.List;

public class ParameterNode extends ASTNode {

    private String sampledId;

    private KType sampledType;

    public ParameterNode(GrammarAST antlrNode) {
        super(antlrNode, new ArrayList<>());
        sampledId = null;
        sampledType = null;
    }

    public String getSampledId() {
        if (sampledId == null) {
            throw new IllegalStateException("Sampled id queried before sampling");
        }

        return sampledId;
    }

    public KType getSampledType() {
        if (sampledType == null) {
            throw new IllegalStateException("Sampled type queried before sampling");
        }

        return sampledType;
    }

    // TODO inline functions
    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        invariant();

        String id = StringUtilities.randomString();
        KType type = ctx.getRandomType();

        // TODO fix symbolics ASAP
        while (type.name().contains("Comparable")) {
            type = ctx.getRandomType();
        }

        this.sampledId = id;
        this.sampledType = type;

        // Kotlin allows for trailing commas
        return new CodeFragment(id + ": " + type.toString() + ",");
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Parameter node with children.");
        }
    }
}
