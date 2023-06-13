package org.fuzzer.grammar.ast.statements;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LoopStatementNode extends ASTNode {

    public LoopStatementNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children,
                             FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, parent, children, stats, cfg);
    }

    public LoopStatementNode(GrammarAST anltrNode, FuzzerStatistics stats, Configuration cfg) {
        super(anltrNode, new LinkedList<>(), stats, cfg);
        List<ASTNode> children = new LinkedList<>();
        // TODO
        this.children = children;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return null;
    }

    @Override
    public void invariant() {

    }
}
