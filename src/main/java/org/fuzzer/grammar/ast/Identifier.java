package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.Constraint;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.StringUtilities;

import java.util.List;

public class Identifier extends LexerNode {

    public Identifier(ASTNodeType type, GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        super(type, antlrNode, parent, children);
    }

    public Identifier() {
        super();
    }

    @Override
    public CodeFragment getSample(Context ctx, List<Constraint> constraints) {
        String newId = StringUtilities.randomIdentifier();
        while (ctx.containsIdentifier(newId))
            newId = StringUtilities.randomIdentifier();
        return new CodeFragment(newId);
    }
}
