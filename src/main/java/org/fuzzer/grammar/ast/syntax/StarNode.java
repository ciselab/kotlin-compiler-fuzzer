package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeConstruct;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StarNode extends SyntaxNode {

    public StarNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, parent, children, null, null);
    }

    public StarNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, null, null);
    }

    @Override
    public List<CodeBlock> getBlocks(RandomNumberGenerator rng, Context ctx) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getStarNodeDist());

        ASTNode nodeToSample = children.get(0);
        List<CodeConstruct> constructs = new LinkedList<>();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            FuzzerStatistics newStats = stats.clone();
            nodeToSample.recordStatistics(newStats);

            constructs.add(nodeToSample.getSample(rng, ctx));

            newStats.stop();
        }

        CodeBlock block = CodeConstruct.aggregateConstructs(constructs);

        return block.split();
    }

    @Override
    public CodeConstruct getSample(RandomNumberGenerator rng, Context ctx) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getStarNodeDist());
        List<CodeConstruct> sampledConstructs = new LinkedList<>();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            CodeConstruct newCode = children.get(0).getSample(rng, ctx);
            sampledConstructs.add(newCode);
        }

        return CodeConstruct.aggregateConstructs(sampledConstructs);
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Star node with more than one child.");
        }
    }
}
