package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.grammar.ast.expressions.SimpleExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.Set;

public class DoWhileNode extends StatementNode {

    public DoWhileNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, maxDepth);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        KType boolType = ctx.getTypeByName("Boolean");
        CodeFragment code = new CodeFragment();
        code.appendToText("do {");

        ExpressionNode conditionNode = new ExpressionNode(antlrNode, maxDepth);
        conditionNode.recordStatistics(stats);

        CodeFragment conditionCode = conditionNode.getRandomExpressionNode(rng).getSampleOfType(rng, ctx, boolType, true, generatedCallableDependencies).first();

        int numberOfStatements = rng.fromGeometric();
        StatementNode stmtNode = new StatementNode(antlrNode, maxDepth).getRandomStatementNode(rng);
        stmtNode.recordStatistics(stats);

        Context innerContext = ctx.clone();

        for (int statement = 0; statement < numberOfStatements; statement++) {
            CodeFragment newCode = stmtNode.getSample(rng, innerContext, generatedCallableDependencies);
            code.extend(newCode);
        }

        code.extend("} while(");
        code.extend(conditionCode);
        code.extend(")");

        // Record this sample
        if (this.stats != null) {
            stats.increment(SampleStructure.DO_WHILE);
        }


        return code;
    }

    @Override
    public void invariant() {

    }
}
