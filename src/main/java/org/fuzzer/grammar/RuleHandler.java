package org.fuzzer.grammar;

import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.grammar.ast.ASTNodeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleHandler {
    private Map<String, GrammarAST> lexerRules;
    private Map<String, GrammarAST> parserRules;

    private final LexerGrammar lexer;

    private final Grammar parser;


    public RuleHandler(LexerGrammar lexerGrammar, Grammar parserGrammar) {
        this.lexer = lexerGrammar;
        this.parser = parserGrammar;

        lexerRules = new HashMap<>();
        parserRules = new HashMap<>();

        for (Map.Entry<String, Rule> tup : parser.rules.entrySet()) {
            transformParserRule(tup.getValue().ast, 0);
        }
    }

    public String getBaseRuleText() {
        return "kotlinFile";
    }

    public GrammarAST getStartingRule() {
        return (GrammarAST) parser.getRule(getBaseRuleText()).ast.getChildren().get(1);
    }

    public GrammarAST getParserRule(String rule) {
        return parserRules.getOrDefault(rule, null);
    }

    private void transformParserRule(GrammarAST node, Integer depth) {
        if (node.getChildren() == null)
            return;

        String rule = null;

        for (GrammarAST child : (List<GrammarAST>) node.getChildren()) {
            ASTNodeType nodeType = ASTNodeType.nodeFromId(child.getType());

            // Get text from productions
            if (depth == 0 && nodeType == ASTNodeType.PRODUCTION) {
                rule = child.getText();
            }

            // Store production
            if (!parserRules.containsKey(child.getText()) && rule != null) {
                parserRules.put(rule, child);
            }

            // Recurse
//            if (!ASTNodeType.isNothing(nodeType)) {
//                transformParserRule(child, depth + 1);
//            }
        }
    }
}
