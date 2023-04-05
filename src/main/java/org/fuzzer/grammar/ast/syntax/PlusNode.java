package org.fuzzer.grammar.ast.syntax;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
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
    public List<CodeSnippet> getSnippets(RandomNumberGenerator rng, Context ctx) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getPlusNodeDist(), cfg.getPlusNodeLb(), cfg.getPlusNodeUb());

        ASTNode nodeToSample = children.get(0);

        List<CodeSnippet> snippets = new LinkedList<>();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            Set<String> dependencyNames = new HashSet<>();

            FuzzerStatistics newStats = stats.clone();
            nodeToSample.recordStatistics(newStats);
            nodeToSample.useConfiguration(cfg);

            CodeFragment newCode = nodeToSample.getSample(rng, ctx, dependencyNames);

            newStats.stop();

            if (newCode.isStructure()) {
                CodeSnippet snippet = new CodeSnippet(newCode, newCode.getName(), dependencyNames, newStats);
                snippets.add(snippet);
            }
        }

        return snippets;
    }

    public PlusNode(List<ASTNode> children) {
        super(null, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getPlusNodeDist(), cfg.getPlusNodeLb(), cfg.getPlusNodeUb());

        CodeFragment code = new CodeFragment();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            CodeFragment newCode = children.get(0).getSample(rng, ctx, generatedCallableDependencies);
            code.extend(newCode);
        }

        return code;
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Plus node with more than one child.");
        }
    }
}

