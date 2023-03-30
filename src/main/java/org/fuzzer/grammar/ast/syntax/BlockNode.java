package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Set;

public class BlockNode extends ASTNode {

    public BlockNode(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        CodeFragment code = new CodeFragment();
        for (ASTNode child : children) {
            code.extend(child.getSample(rng, ctx, generatedCallableDependencies));
        }

        return code;
    }

    @Override
    public void invariant() {
        if (children.isEmpty()) {
            throw new IllegalStateException("Block node with no children.");
        }
    }
}
