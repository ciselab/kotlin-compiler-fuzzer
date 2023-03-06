package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.ExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;

public class DoWhileNode extends ASTNode {

    private int maxDepth;

    public DoWhileNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new LinkedList<>());
        children.add(new StatementNode(antlrNode, maxDepth));

        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType boolType = ctx.getTypeByName("Boolean");
        CodeFragment code = new CodeFragment();
        code.appendToText("do {");

        CodeFragment conditionCode = new ExpressionNode(antlrNode, maxDepth).getSampleOfType(rng, ctx, boolType, true).first();

        int numberOfStatements = rng.fromGeometric();
        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth);

        for (int statement = 0; statement < numberOfStatements; statement++) {
            CodeFragment newCode = stmtNode.getSample(rng, ctx);
            code.extend(newCode);
        }

        code.extend("} while(");
        code.extend(conditionCode);
        code.extend(")");
    }

    @Override
    public void invariant() {

    }
}
