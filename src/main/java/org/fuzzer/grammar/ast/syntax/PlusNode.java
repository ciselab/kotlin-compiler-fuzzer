package org.fuzzer.grammar.ast.syntax;

import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeConstruct;
import org.fuzzer.search.chromosome.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PlusNode extends SyntaxNode {
    @Override
    public List<CodeBlock> getBlocks(RandomNumberGenerator rng, Context ctx) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getPlusNodeDist());

        ASTNode nodeToSample = children.get(0);
        List<CodeConstruct> constructs = new LinkedList<>();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            FuzzerStatistics newStats = stats.clone();
            nodeToSample.recordStatistics(newStats);

             constructs.add(nodeToSample.getSample(rng, ctx));

            newStats.stop();
        }

        CodeBlock block = CodeConstruct.aggregateConstructs(constructs);

        return null;
    }

    public PlusNode(List<ASTNode> children, Configuration cfg) {
        super(null, children, null, cfg);
    }

    @Override
    public CodeConstruct getSample(RandomNumberGenerator rng, Context ctx) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getPlusNodeDist());
        List<CodeConstruct> sampledConstructs = new LinkedList<>();

        // TODO: fix bug that allows context to sample callables requiring unsambplable types
        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            try {
                CodeConstruct newCode = children.get(0).getSample(rng, ctx);
                sampledConstructs.add(newCode);
            } catch (IllegalStateException e) {
                System.out.println("Error sampling child of PlusNode: " + e.getMessage());
                System.out.println("Proceeding to next iteration with empty sample...");
            }
        }

        return CodeConstruct.aggregateConstructs(sampledConstructs);
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Plus node with more than one child.");
        }
    }
}

