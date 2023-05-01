package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
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
    public List<CodeSnippet> getSnippets(RandomNumberGenerator rng, Context ctx) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getStarNodeDist());

        ASTNode nodeToSample = children.get(0);

        List<CodeSnippet> snippets = new LinkedList<>();

        for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
            Set<String> dependencyNames = new HashSet<>();

            FuzzerStatistics newStats = stats.clone();
            nodeToSample.recordStatistics(newStats);

            CodeFragment newCode = nodeToSample.getSample(rng, ctx, dependencyNames);

            newStats.stop();

            if (newCode.isStructure()) {
                CodeSnippet snippet = new CodeSnippet(newCode, newCode.getName(), dependencyNames, newStats);
                snippets.add(snippet);
            }
        }

        return snippets;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        invariant();

        int numberOfSamples = rng.fromDiscreteDistribution(cfg.getStarNodeDist());

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
            throw new IllegalStateException("Star node with more than one child.");
        }
    }
}
