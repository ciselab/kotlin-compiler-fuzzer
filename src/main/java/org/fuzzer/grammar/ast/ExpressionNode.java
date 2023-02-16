package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ExpressionNode extends ASTNode {

    private final int maxDepth;

    public ExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new ArrayList<>());
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType sampledType = ctx.getRandomType();
        return getSampleOfType(rng, ctx, sampledType);
    }

    public CodeFragment getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type) {

        try {
            int depth = 0;
            KCallable baseCallable = getCallableOfType(type, depth++, ctx, rng);
            Tree<KCallable> rootNode = new Tree<>(baseCallable);

            rootNode = sampleTypedCallables(rootNode, depth, ctx, rng);
            verifyCallableCompatibility(rootNode, ctx);
            String expression = rootNode.getValue().call(ctx);
            return new CodeFragment(expression);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void invariant() {

    }

    public Tree<KCallable> sampleTypedCallables(Tree<KCallable> currentNode, int depth,
                                                Context ctx, RandomNumberGenerator rng) throws CloneNotSupportedException {
        KCallable currentCallable = currentNode.getValue();
        if (currentCallable.isTerminal()) {
            return currentNode;
        }

        if (depth >= this.maxDepth) {
            throw new IllegalStateException("Node exceeded maximum depth during sampling.");
        }

        // First, sample callables for this level
        for (KType inputType : currentNode.getValue().getInputTypes()) {
            KCallable child = getCallableOfType(inputType, depth, ctx, rng);
            currentNode.addChild(child);
        }

        assert currentNode.getValue().getInputTypes().size() == currentNode.getChildren().size();

        // Recursively sample callables as inputs for all children
        for (Tree<KCallable> childNode : currentNode.getChildren()) {
            sampleTypedCallables(childNode, depth + 1, ctx, rng);
        }

        return currentNode;
    }

    public KCallable getCallableOfType(KType type, int depth, Context ctx, RandomNumberGenerator rng) throws CloneNotSupportedException {
        boolean sampleConsumerCallable = (depth < maxDepth - 1) && rng.randomBoolean(0.25);
        KCallable callable = null;

        if (sampleConsumerCallable) {
            try {
                callable = ctx.randomConsumerCallable(type);
            } catch (IllegalArgumentException ignored) {}

            if (callable != null) {
                return callable;
            }
        }

        try {
            callable = ctx.randomTerminalCallableOfType(type);
        } catch (IllegalArgumentException ignored) {}

        if (callable != null) {
            return callable;
        }

        if (depth >= maxDepth - 1) {
            throw new IllegalStateException("Max depth exceeded, but no terminal found.");
        }

        // Rolled false, and no terminal callable found.
        try {
            callable = ctx.randomConsumerCallable(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("No callable found.");
        }

        return callable;
    }

    private void verifyCallableCompatibility(Tree<KCallable> callableTree, Context ctx) throws CloneNotSupportedException {
        for (Tree<KCallable> child : callableTree.getChildren()) {
            verifyCallableCompatibility(child, ctx);
        }
        List<KCallable> childrenCallables = List.copyOf(callableTree.getChildrenValues());
        KCallable callable = callableTree.getValue();

        if (callable.requiresOwner()) {
            // TODO implement a class member callable
            KMethod methodCallable = (KMethod) callable;
            KType ownerType = ctx.getTypeByName(methodCallable.getOwnerType().name());
            KCallable owner = sampleOwnerCallableOfType(ownerType, ctx);
            callable.call(ctx, owner, childrenCallables);
        } else {
            callable.call(ctx, null, childrenCallables);
        }


    }

    private KCallable sampleOwnerCallableOfType(KType type, Context ctx) throws CloneNotSupportedException {
        // Sample callables that are either identifiers or constructors
        Predicate<KCallable> constructorOrIdOrAnon = kCallable -> kCallable instanceof KConstructor ||
                kCallable instanceof KIdentifierCallable ||
                kCallable instanceof KAnonymousCallable;

        KCallable sampledOwner = null;
        try {
            sampledOwner = ctx.randomCallableOfType(type, constructorOrIdOrAnon);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Cannot sample an owner of type: " + type);
        }

        List<KCallable> sampledOwnerInput = new ArrayList<>();

        for (KType inputType : sampledOwner.getInputTypes()) {
            sampledOwnerInput.add(sampleOwnerCallableOfType(inputType, ctx));
        }

        sampledOwner.call(ctx, null, sampledOwnerInput);
        return sampledOwner;
    }
}
