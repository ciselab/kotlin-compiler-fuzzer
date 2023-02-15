package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.List;

public class ModifiersNode extends ASTNode {
    public ModifiersNode(GrammarAST antlrNode) {
        super(antlrNode, new ArrayList<>());
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return null;
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Modifiers node with children.");
        }
    }
}
