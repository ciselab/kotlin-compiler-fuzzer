package org.fuzzer.generator;

import org.antlr.runtime.RecognitionException;

import java.io.*;
import java.util.*;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.representations.callables.KIdentifierCallable;
import org.fuzzer.representations.context.Context;
import org.fuzzer.grammar.ast.ASTNodeType;
import org.fuzzer.grammar.RuleHandler;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.StringUtilities;
import org.fuzzer.utils.Tree;

public class CodeGenerator {
//    private final RandomNumberGenerator rng;
//
//    private final RuleHandler ruleHandler;
//
//    private Context ctx;
//
//    private int maxDepth;
//
//    public CodeGenerator(RuleHandler ruleHandler, RandomNumberGenerator rng, int maxDepth) throws IOException, RecognitionException {
//
//        this.ruleHandler = ruleHandler;
//
//        this.rng = rng;
//
//        this.ctx = new Context(rng);
//
//        this.maxDepth = maxDepth;
//    }
//
//    public CodeFragment sampleAssignment() throws CloneNotSupportedException {
//        KType type = null;
//        String id = null;
//        Boolean sampleExisting = ctx.hasAnyVariables() && rng.randomBoolean();
//
//        if (sampleExisting) {
//            id = ctx.randomIdentifier();
//            type = ctx.typeOfIdentifier(id);
//        } else {
//            id = StringUtilities.randomIdentifier();
//            while (ctx.containsIdentifier(id)) {
//                id = StringUtilities.randomIdentifier();
//            }
//            type = ctx.getRandomType();
//        }
//
//        int depth = 0;
//
//        KCallable baseCallable = getCallableOfType(type, depth++);
//        Tree<KCallable> rootNode = new Tree<>(baseCallable);
//
//        rootNode = sampleTypedCallables(rootNode, depth);
//        verifyCallableCompatibility(rootNode);
//
//        String rhs = rootNode.getValue().call(ctx);
//        String lhs = sampleExisting ? id : ("var " + id + ": " + type.getName());
//
//        if (!sampleExisting) {
//            ctx.addIdentifier(id, new KIdentifierCallable(id, type));
//        }
//
//        return new CodeFragment(lhs + "=" + rhs);
//
//    }
//
//    public Tree<KCallable> sampleTypedCallables(Tree<KCallable> currentNode, int depth) throws CloneNotSupportedException {
//        KCallable currentCallable = currentNode.getValue();
//        if (currentCallable.isTerminal()) {
//            return currentNode;
//        }
//
//        if (depth >= this.maxDepth) {
//            throw new IllegalStateException("Node exceeded maximum depth during sampling.");
//        }
//
//        // First, sample callables for this level
//        for (KTypePlaceHolder inputType : currentNode.getValue().getInputTypes()) {
//            KCallable child = getCallableOfType(inputType, depth);
//            currentNode.addChild(child);
//        }
//
//        assert currentNode.getValue().getInputTypes().size() == currentNode.getChildren().size();
//
//        // Recursively sample callables as inputs for all children
//        for (Tree<KCallable> childNode : currentNode.getChildren()) {
//            sampleTypedCallables(childNode, depth + 1);
//        }
//
//        return currentNode;
//    }
//
//    public KCallable getCallableOfType(KTypePlaceHolder type, int depth) throws CloneNotSupportedException {
//        Boolean sampleConsumerCallable = (depth < maxDepth - 1) && rng.randomBoolean(0.25);
//        Optional<KCallable> callable;
//
//        if (sampleConsumerCallable) {
//            callable = ctx.randomConsumerCallable(type);
//            if (callable.isPresent()) {
//                return callable.get();
//            }
//        }
//
//        callable = ctx.randomTerminalCallableOfType(type);
//        if (callable.isPresent()) {
//            return callable.get();
//        }
//
//        if (depth >= maxDepth) {
//            throw new IllegalStateException("Max depth exceeded, but no terminal found.");
//        }
//
//        // Rolled false, and no terminal callable found.
//        callable = ctx.randomConsumerCallable(type);
//        if (callable.isPresent()) {
//            return callable.get();
//        }
//
//        throw new IllegalStateException("No callable found.");
//    }
//
//    private void verifyCallableCompatibility(Tree<KCallable> callableTree) {
//        for (Tree<KCallable> child : callableTree.getChildren()) {
//            verifyCallableCompatibility(child);
//        }
//        List<KCallable> childrenCallables = List.copyOf(callableTree.getChildrenValues());
//        callableTree.getValue().call(ctx, childrenCallables);
//    }
//
//    public CodeFragment sampleFromGrammar(GrammarAST ast) {
//        return sampleFromGrammar(ast, 0, new CodeFragment());
//    }
//
//    public CodeFragment sampleFromGrammar(GrammarAST ast, int depth, CodeFragment code) {
//        if (ast == null)
//            return code;
//
//        if (ASTNodeType.shouldHandle(ast)) {
//            switch (ASTNodeType.nodeFromId(ast.getType())) {
//                case BLOCK, RULE_MODIFIERS -> {
//                    // Skip such nodes (no content)
//                    assert (ast.getChildren().size() == 1);
//                    return sampleFromGrammar((GrammarAST) ast.getChild(0), depth + 1, code);
//                }
//                case ALT -> {
//                    for (GrammarAST child : (List<GrammarAST>) ast.getChildren()) {
//                        code = sampleFromGrammar(child, depth + 1, code);
//                    }
//                    return code;
//                }
//                case SET -> {
//                    GrammarAST selectedChild = (GrammarAST) rng.selectFromList(ast.getChildren());
//                    return sampleFromGrammar(selectedChild, depth + 1, code);
//                }
//                case AT_MOST_ONCE -> {
//                    return rng.randomBoolean() ? sampleFromGrammar((GrammarAST) ast.getChildren().get(0), depth + 1, code) : code;
//                }
//                case AT_LEAST_ONCE -> {
//                    assert (ast.getChildren().size() == 1);
//                    int numberOfSamples = 1 + rng.fromGeometric();
//                    for (int i = 0; i < numberOfSamples; i++) {
//                        code = sampleFromGrammar((GrammarAST) ast.getChild(0), depth + 1, code);
//                    }
//                    return code;
//                }
//                case UNKNOWN -> {
//                    assert (ast.getChildren().size() == 1);
//                    int numberOfSamples = rng.fromGeometric();
//                    for (int i = 0; i < numberOfSamples; i++) {
//                        code = sampleFromGrammar((GrammarAST) ast.getChild(0), depth + 1, code);
//                    }
//                    return code;
//                }
//                case LITERAL -> {
//                    code.appendToText(StringUtilities.correctEscapedCharacter(ast.getText().substring(1, ast.getText().length() - 1)));
//                    return code;
//                }
//                case PRODUCTION -> {
//                    return sampleFromGrammar(ruleHandler.getParserRule(ast.getText()), depth + 1, code); // Context needed
//                }
//                case TOKEN -> {
//                    System.out.println("Token encountered");
//                    System.out.println(ast.getText());
//                    if ("Identifier".equals(ast.getText()))
//                        code.appendToText("id"); // context needed
//                    code.appendToText(ast.getText());
//                }
//                default -> {
//                }
//            }
//        }
//        if (ast.getChildren() == null)
//            return code;
//
//        List<GrammarAST> children = (List<GrammarAST>) ast.getChildren();
//        GrammarAST nextNode = children.get(rng.fromUniformDiscrete(0, children.size() - 1));
//
//        return sampleFromGrammar(nextNode, depth + 1, code);
//    }
}
