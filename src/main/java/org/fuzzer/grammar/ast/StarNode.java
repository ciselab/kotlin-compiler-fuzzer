package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Set;

public class StarNode extends ASTNode {

    public StarNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        super(antlrNode, parent, children);
    }

    public StarNode(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    public StarNode(List<ASTNode> children) {
        super(null, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        invariant();

        int numberOfSamples = rng.fromGeometric();

        CodeFragment code = new CodeFragment();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            CodeFragment newCode = children.get(0).getSample(rng, ctx, generatedCallableDependencies);
            code.extend(newCode);
        }

        return code;
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Star node with more than one child.");
        }
    }
}
