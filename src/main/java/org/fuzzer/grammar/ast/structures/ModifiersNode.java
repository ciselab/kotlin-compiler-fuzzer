package org.fuzzer.grammar.ast.structures;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModifiersNode extends ASTNode {
    public ModifiersNode(GrammarAST antlrNode) {
        super(antlrNode, new ArrayList<>(), null, null);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        return null;
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Modifiers node with children.");
        }
    }
}
