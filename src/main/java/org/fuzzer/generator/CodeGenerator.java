package org.fuzzer.generator;

import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.representations.context.Context;
import org.fuzzer.grammar.ast.ASTNodeType;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.StringUtilities;

public class CodeGenerator {
    private final RandomNumberGenerator rng;

    private final RuleHandler ruleHandler;

    private Context ctx;

    private int maxDepth;

    public CodeGenerator(RuleHandler ruleHandler, RandomNumberGenerator rng, int maxDepth, Context ctx) throws RecognitionException {

        this.ruleHandler = ruleHandler;

        this.rng = rng;

        this.ctx = ctx;

        this.maxDepth = maxDepth;
    }
    public CodeFragment sampleFromGrammar(GrammarAST ast) {
        return sampleFromGrammar(ast, 0, new CodeFragment());
    }

    public CodeFragment sampleFromGrammar(GrammarAST ast, int depth, CodeFragment code) {
        if (ast == null)
            return code;

        if (ASTNodeType.shouldHandle(ast)) {
            switch (ASTNodeType.nodeFromId(ast.getType())) {
                case BLOCK, RULE_MODIFIERS -> {
                    // Skip such nodes (no content)
                    assert (ast.getChildren().size() == 1);
                    return sampleFromGrammar((GrammarAST) ast.getChild(0), depth + 1, code);
                }
                case ALT -> {
                    for (GrammarAST child : (List<GrammarAST>) ast.getChildren()) {
                        code = sampleFromGrammar(child, depth + 1, code);
                    }
                    return code;
                }
                case SET -> {
                    GrammarAST selectedChild = (GrammarAST) rng.selectFromList(ast.getChildren());
                    return sampleFromGrammar(selectedChild, depth + 1, code);
                }
                case AT_MOST_ONCE -> {
                    return rng.randomBoolean() ? sampleFromGrammar((GrammarAST) ast.getChildren().get(0), depth + 1, code) : code;
                }
                case AT_LEAST_ONCE -> {
                    assert (ast.getChildren().size() == 1);
                    int numberOfSamples = 1 + rng.fromGeometric();
                    for (int i = 0; i < numberOfSamples; i++) {
                        code = sampleFromGrammar((GrammarAST) ast.getChild(0), depth + 1, code);
                    }
                    return code;
                }
                case UNKNOWN -> {
                    assert (ast.getChildren().size() == 1);
                    int numberOfSamples = rng.fromGeometric();
                    for (int i = 0; i < numberOfSamples; i++) {
                        code = sampleFromGrammar((GrammarAST) ast.getChild(0), depth + 1, code);
                    }
                    return code;
                }
                case LITERAL -> {
                    code.appendToText(StringUtilities.correctEscapedCharacter(ast.getText().substring(1, ast.getText().length() - 1)));
                    return code;
                }
                case PRODUCTION -> {
                    return sampleFromGrammar(ruleHandler.getParserRule(ast.getText()), depth + 1, code); // Context needed
                }
                case TOKEN -> {
                    System.out.println("Token encountered");
                    System.out.println(ast.getText());
                    if ("Identifier".equals(ast.getText()))
                        code.appendToText("id"); // context needed
                    code.appendToText(ast.getText());
                }
                default -> {
                }
            }
        }
        if (ast.getChildren() == null)
            return code;

        List<GrammarAST> children = (List<GrammarAST>) ast.getChildren();
        GrammarAST nextNode = children.get(rng.fromUniformDiscrete(0, children.size() - 1));

        return sampleFromGrammar(nextNode, depth + 1, code);
    }
}
