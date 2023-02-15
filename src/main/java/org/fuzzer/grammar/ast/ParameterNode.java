package org.fuzzer.grammar.ast;

import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

public class ParameterNode extends ASTNode {
    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return null;
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Parameter node with children.");
        }
    }
}
