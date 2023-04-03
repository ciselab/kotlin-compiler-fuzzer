package org.fuzzer.grammar.ast.syntax;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;

public abstract class SyntaxNode extends ASTNode {

    public SyntaxNode(GrammarAST antlrNode, ASTNode parent, List<ASTNode> children) {
        super(antlrNode, parent, children);
    }

    public SyntaxNode(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    abstract public List<CodeSnippet> getSnippets(RandomNumberGenerator rng, Context ctx);
}
