package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Optional;

public class OptNode extends ASTNode {

    public OptNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        super(antlrNode, parent, children);
    }

    public OptNode(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        if (rng.randomBoolean()) {
            return children.get(0).getSample(rng, ctx);
        } else {
            return new CodeFragment();
        }
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Optional node with more than one child.");
        }
    }
}
