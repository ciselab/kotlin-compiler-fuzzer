package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.grammar.ast.expressions.SimpleExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;

public class DoWhileNode extends StatementNode {

    public DoWhileNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType boolType = ctx.getTypeByName("Boolean");
        CodeFragment code = new CodeFragment();
        code.appendToText("do {");

        CodeFragment conditionCode = new ExpressionNode(antlrNode, maxDepth).getRandomExpressionNode(rng).getSampleOfType(rng, ctx, boolType, true).first();

        int numberOfStatements = rng.fromGeometric();
        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth).getRandomStatementNode(rng);

        for (int statement = 0; statement < numberOfStatements; statement++) {
            CodeFragment newCode = stmtNode.getSample(rng, ctx);
            code.extend(newCode);
        }

        code.extend("} while(");
        code.extend(conditionCode);
        code.extend(")");

        return code;
    }

    @Override
    public void invariant() {

    }
}
