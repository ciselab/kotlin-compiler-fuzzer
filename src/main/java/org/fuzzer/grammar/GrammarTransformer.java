package org.fuzzer.grammar;

import org.antlr.v4.tool.Alternative;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.grammar.ast.*;

import java.util.*;

public class GrammarTransformer {

    private final LexerGrammar lexerGrammar;

    private final Grammar parserGrammar;

    private final String baseRuleName = "kotlinFile";

    public GrammarTransformer(LexerGrammar lexerGrammar, Grammar parserGrammar) {
        this.lexerGrammar = lexerGrammar;
        this.parserGrammar = parserGrammar;
    }

    public String getBaseRuleName() {
        return baseRuleName;
    }

    public Rule getBaseRule() {
        return parserGrammar.getRule(getBaseRuleName());
    }

    public ASTNode transformGrammar() {
        return transformGrammar(getBaseRule());
    }

    public ASTNode transformGrammar(Rule rule) {

        switch (rule.name) {
            case RuleName.kotlinFile -> {
                List<ASTNode> altNodes = new ArrayList<>();

                for (Alternative alt : rule.alt) {

                    // Skip nulls
                    if (alt == null) {
                        continue;
                    }

                    altNodes.add(transformGrammar(alt.ast));
                }

                return new AltNode(null, null, altNodes);
            }
            default -> throw new IllegalStateException("Unexpected value: " + rule.name);
        }
    }

    public ASTNode transformGrammar(GrammarAST ast) {
        switch (ast.getText()) {

            case RuleName.kotlinFile -> {
                throw new RuntimeException();
            }

            // |
            case RuleName.ALT -> {
                List<ASTNode> altNodes = new ArrayList<>();

                for (Object child : ast.getChildren()) {

                    GrammarAST childNode = (GrammarAST) child;

                    altNodes.add(transformGrammar(childNode));
                }

                return new AltNode(null, altNodes);
            }

            // ?
            case RuleName.OPTIONAL_BLOCK -> {
                GrammarAST childNode = (GrammarAST) ast.getChild(0);

                List<ASTNode> children = new ArrayList<>();

                children.add(transformGrammar(childNode));

                return new OptNode(ast, children);
            }

            // *
            case RuleName.STAR_BLOCK -> {
                GrammarAST childNode = (GrammarAST) ast.getChild(0);

                List<ASTNode> children = new ArrayList<>();

                children.add(transformGrammar(childNode));

                return new StarNode(ast, children);
            }

            case RuleName.BLOCK -> {
                // Ignore the block
                return transformGrammar((GrammarAST) ast.getChild(0));
            }

            // Placeholder
            default -> {
                return new TextNode(ast, ast.token.toString());
            }
        }
    }
}
