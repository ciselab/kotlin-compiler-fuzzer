package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public abstract class SyntaxNode extends ASTNode {

    public SyntaxNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, parent, children, stats, cfg);
    }

    public SyntaxNode(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, stats, cfg);
    }

    abstract public List<CodeBlock> getBlocks(RandomNumberGenerator rng, Context ctx);
}
