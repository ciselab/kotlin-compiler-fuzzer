package org.fuzzer.grammar;

import org.antlr.v4.tool.Alternative;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.grammar.ast.*;
import org.fuzzer.grammar.ast.structures.FunctionDecl;
import org.fuzzer.grammar.ast.syntax.*;

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

                return new AltNode(null, null, altNodes, null, null);
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

                // If only one option, skip the ALT node
                if (ast.getChildren().size() == 1) {
                    return transformGrammar((GrammarAST) ast.getChild(0));
                }

                for (Object child : ast.getChildren()) {

                    GrammarAST childNode = (GrammarAST) child;

                    altNodes.add(transformGrammar(childNode));
                }

                return new AltNode(null, altNodes, null, null);
            }

            // ?
            case RuleName.OPTIONAL_BLOCK -> {
                GrammarAST childNode = (GrammarAST) ast.getChild(0);

                List<ASTNode> children = new ArrayList<>();

                children.add(transformGrammar(childNode));

                return new OptNode(ast, children, null, null);
            }

            // *
            case RuleName.STAR_BLOCK -> {
                GrammarAST childNode = (GrammarAST) ast.getChild(0);

                List<ASTNode> children = new ArrayList<>();

                children.add(transformGrammar(childNode));

                return new StarNode(ast, children, null, null);
            }

            case RuleName.BLOCK -> {
                if (ast.getChildren().size() == 1) {
                    // Skip block if single instruciton
                    return transformGrammar((GrammarAST) ast.getChild(0));
                }

                List<ASTNode> children = new ArrayList<>();

                for (Object child : ast.getChildren()) {
                    GrammarAST childNode = (GrammarAST) child;

                    children.add(transformGrammar(childNode));
                }

                return new BlockNode(ast, children, null, null);
            }

            case RuleName.SEMIS, RuleName.SEMI -> {
                return new TextNode(ast, System.lineSeparator(), null, null);
            }

            // RuleRefAST have to be retrieved from by `getRule()`
            case RuleName.TOP_LEVEL_OBJ -> {
                Rule topLevelRule = parserGrammar.getRule(ast.getText());

                // Children: topLevelObject, BLOCK
                GrammarAST topLevelAST = topLevelRule.ast;
                return transformGrammar((GrammarAST) topLevelAST.getChild(1));
            }

            case RuleName.DECLARATION -> {
                Rule topLevelRule = parserGrammar.getRule(ast.getText());

                // Children: declaration (empty), BLOCK -> 5x ALT
                GrammarAST declAST = topLevelRule.ast;
                List<ASTNode> declOptions = new ArrayList<>();

                for (Object declType : ((GrammarAST) declAST.getChild(1)).getChildren()) {
                    declOptions.add(transformGrammar((GrammarAST) declType));
                }

                return new AltNode(ast, declOptions, null, null);
            }

            case RuleName.FUNC_DECL -> {
                return new FunctionDecl(ast, new ArrayList<>(), null, null);
            }

            // Placeholder
            default -> {
                return new TextNode(ast, ast.token.toString(), null, null);
            }
        }
    }
}
