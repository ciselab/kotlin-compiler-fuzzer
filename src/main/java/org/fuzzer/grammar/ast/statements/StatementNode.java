package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.*;
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
        if (rng.fromUniformContinuous(0.0, 1.0) < cfg.getSimplicityBias()) {
            return createStatementNodeFromStructure(SampleStructure.SIMPLE_STMT);
        } else {
            SampleStructure selectedNode = rng.fromProbabilityTable(cfg.getStatementProbabilityTable());
            return createStatementNodeFromStructure(selectedNode);
        }
    }

    private StatementNode createStatementNodeFromStructure(SampleStructure structure) {
        StatementNode node;

        switch (structure) {
            case ASSIGNMENT -> node = new AssignmentNode(antlrNode, maxDepth);
            case DO_WHILE -> node = new DoWhileNode(antlrNode, maxDepth);
            case SIMPLE_STMT -> node = new SimpleStatementNode(antlrNode, maxDepth);
            default ->
                    throw new IllegalArgumentException("Cannot create statement node of structure: " + structure);
        }

        node.useConfiguration(cfg);
        node.recordStatistics(stats);

        return node;
    }

    @Override
    public void invariant() {

    }
}
