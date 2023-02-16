package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public class FunctionDecl extends ASTNode {
    public FunctionDecl(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return null;
    }

    @Override
    public void invariant() {

    }
}
