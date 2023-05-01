package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
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

    public StatementNode(GrammarAST antlrNode, int maxDepth, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, new LinkedList<>(), stats, cfg);
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

        switch (structure) {
            case ASSIGNMENT -> {
                return new AssignmentNode(antlrNode, maxDepth, stats, cfg);
            }
            case DO_WHILE -> {
                return new DoWhileNode(antlrNode, maxDepth, stats, cfg);
            }
            case SIMPLE_STMT -> {
                return new SimpleStatementNode(antlrNode, maxDepth, stats, cfg);
            }
            default ->
                    throw new IllegalArgumentException("Cannot create statement node of structure: " + structure);
        }
    }

    @Override
    public void invariant() {

    }
}
