package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeConstruct;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BlockNode extends ASTNode {

    public BlockNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, stats, cfg);
    }

    @Override
    public CodeConstruct getSample(RandomNumberGenerator rng, Context ctx) {
        List<CodeConstruct> sampledConstructs = new LinkedList<>();
        for (ASTNode child : children) {
            sampledConstructs.add(child.getSample(rng, ctx));
        }

        return CodeConstruct.aggregateConstructs(sampledConstructs);
    }

    @Override
    public void invariant() {
        if (children.isEmpty()) {
            throw new IllegalStateException("Block node with no children.");
        }
    }
}
