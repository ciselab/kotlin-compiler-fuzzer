package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OptNode extends ASTNode {

    public OptNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, parent, children, stats, cfg);
    }

    public OptNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, stats, cfg);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        if (rng.randomBoolean()) {
            return children.get(0).getSample(rng, ctx, generatedCallableDependencies);
        } else {
            return new CodeFragment();
        }
    }

    @Override
    public void invariant() {
        if (children.size() != 1) {
            throw new IllegalStateException("Optional node with more than one child.");
        }
    }
}
