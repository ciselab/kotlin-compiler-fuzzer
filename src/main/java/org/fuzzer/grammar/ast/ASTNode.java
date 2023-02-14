package org.fuzzer.grammar.ast;

import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.context.Context;

import java.util.List;

public interface ASTNode {
    public CodeFragment getSample(Context ctx);

    public boolean invariant();

    public List<ASTNode> getChildren();

    public ASTNode getParent();
}
