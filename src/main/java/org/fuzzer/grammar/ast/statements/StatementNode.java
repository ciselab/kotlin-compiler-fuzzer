package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.SimpleExpressionNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StatementNode extends ASTNode {

    protected final int maxDepth;

    public StatementNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new LinkedList<>());
        this.maxDepth = maxDepth;
    }
    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        return getRandomStatementNode(rng).getSample(rng, ctx, generatedCallableDependencies);
    }

    public StatementNode getRandomStatementNode(RandomNumberGenerator rng) {
        List<StatementNode> alternatives = new LinkedList<>();
        alternatives.add(new AssignmentNode(antlrNode, this.maxDepth));
        alternatives.add(new DoWhileNode(antlrNode, maxDepth));
        alternatives.add(new SimpleStatementNode(antlrNode, maxDepth));

        StatementNode selectedNode;
        // Forcibly simplify the sampling
        if (rng.fromUniformContinuous(0.0, 1.0) < 0.4) {
            selectedNode = alternatives.get(alternatives.size() - 1);
        } else {
            selectedNode = alternatives.get(rng.fromUniformDiscrete(0, alternatives.size() - 1));
        }
        selectedNode.recordStatistics(stats);

        return selectedNode;
    }

    @Override
    public void invariant() {

    }
}
