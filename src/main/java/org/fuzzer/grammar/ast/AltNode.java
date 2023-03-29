package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AltNode extends ASTNode {

    public AltNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        super(antlrNode, parent, children);
    }

    public AltNode(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        invariant();

        ASTNode nodeToSample = children.get(rng.fromUniformDiscrete(0, children.size() - 1));
        return nodeToSample.getSample(rng, ctx, generatedCallableDependencies);
    }

    @Override
    public void invariant() {
        boolean holds = !children.isEmpty();

        if (!holds) {
            throw new IllegalStateException("Alt node with no alternatives encountered.");
        }
    }
}
