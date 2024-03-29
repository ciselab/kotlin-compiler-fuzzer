package org.fuzzer.grammar.ast.structures;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.Set;

public class ParameterNode extends ASTNode {

    private String sampledId;

    private KType sampledType;

    public ParameterNode(GrammarAST antlrNode) {
        super(antlrNode, new LinkedList<>(), null, null);
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

        String id = ctx.getNewIdentifier();
        KType type = ctx.getRandomType();

        this.sampledId = id;
        this.sampledType = type;

        // Kotlin allows for trailing commas
        return CodeFragment.textCodeFragment(id.replaceAll("\"", "") + ": " + type.codeRepresentation() + ",");
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Parameter node with children.");
        }
    }
}
