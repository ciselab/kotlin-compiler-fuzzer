package org.fuzzer.grammar.ast;

import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.Constraint;
import org.fuzzer.grammar.Context;

import java.util.List;

public interface ASTNode {
    public CodeFragment getSample(Context ctx, List<Constraint> constraints);

    public boolean invariant();

    public List<ASTNode> getChildren();

    public ASTNode getParent();
}
