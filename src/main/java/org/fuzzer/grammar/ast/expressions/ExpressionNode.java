package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tree;
import org.fuzzer.utils.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class ExpressionNode extends ASTNode {

    protected final int maxDepth;

    public ExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new ArrayList<>());
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        return getRandomExpressionNode(rng).getSample(rng, ctx);
    }

    public ExpressionNode getRandomExpressionNode(RandomNumberGenerator rng) {
        List<ExpressionNode> alternatives = new ArrayList<>(List.of(new ExpressionNode[]{new IfExpressionNode(antlrNode, maxDepth), new SimpleExpressionNode(antlrNode, maxDepth)}));

        if (rng.randomBoolean()) {
            return alternatives.get(alternatives.size() - 1);
        }

        return alternatives.get(rng.fromUniformDiscrete(0, alternatives.size() - 1));
    }

    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type, boolean allowSubtypes) {
        try {
            int depth = 0;

            KCallable baseCallable = getCallableOfType(type, depth++, ctx, rng);
            Tree<KCallable> rootNode = new Tree<>(baseCallable);

            KType returnType = baseCallable.getReturnType();

            if (returnType.name().contains("Comparable")) {
                System.out.println("break");
            }

            List<KType> typeParameterInstances = ctx.getParameterInstances(type, returnType);

            if (typeParameterInstances.size() != type.getGenerics().size()) {
                throw new IllegalStateException("Sampling subtypes failed");
            }

            rootNode = sampleTypedCallables(rootNode, depth, ctx, rng);
            verifyCallableCompatibility(rootNode, ctx, allowSubtypes);
            String expression = rootNode.getValue().call(ctx);

            if (this.stats != null) {
                stats.increment(SampleStructure.STATEMENT);
            }

            CodeFragment code = new CodeFragment(expression);

            return new Tuple<>(code,
                    new Tuple<>(returnType, typeParameterInstances.stream()
                    .map(t -> ctx.getTypeByName(t.name())).toList()));

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Tree<KCallable> sampleTypedCallables(Tree<KCallable> currentNode, int depth,
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

    private KCallable getCallableOfType(KType type, int depth, Context ctx, RandomNumberGenerator rng) throws CloneNotSupportedException {
        boolean sampleConsumerCallable = (depth < maxDepth - 1) && rng.randomBoolean(0.25);
        KCallable callable = null;
        boolean allowSubtypes = true;

        if (sampleConsumerCallable) {
            try {
                callable = ctx.randomConsumerCallable(type, allowSubtypes);
            } catch (IllegalArgumentException ignored) {}

            if (callable != null) {
                return callable;
            }
        }

        try {
            callable = ctx.randomTerminalCallableOfType(type, allowSubtypes);
        } catch (IllegalArgumentException ignored) {}

        if (callable != null) {
            return callable;
        }

        if (depth >= maxDepth - 1) {
            throw new IllegalStateException("Max depth exceeded, but no terminal callable of type " + type + " found.");
        }

        // Rolled false, and no terminal callable found.
        try {
            callable = ctx.randomConsumerCallable(type, allowSubtypes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("No callable found for type: " + type);
        }

        return callable;
    }

    private void verifyCallableCompatibility(Tree<KCallable> callableTree, Context ctx, boolean allowSubtypes) throws CloneNotSupportedException {
        for (Tree<KCallable> child : callableTree.getChildren()) {
            verifyCallableCompatibility(child, ctx, allowSubtypes);
        }
        List<KCallable> childrenCallables = List.copyOf(callableTree.getChildrenValues());
        KCallable callable = callableTree.getValue();

        if (callable.requiresOwner()) {
            // TODO implement a class member callable
            if (callable instanceof KConstructor) {
                callable.call(ctx, null, childrenCallables);
            // Otherwise, it is a method
            } else {
                KMethod methodCallable = (KMethod) callable;
                KType ownerType = ctx.getTypeByName(methodCallable.getOwnerType().name());
                KCallable owner = sampleOwnerCallableOfType(ownerType, ctx, allowSubtypes);
                callable.call(ctx, owner, childrenCallables);
            }

        } else {
            callable.call(ctx, null, childrenCallables);
        }


    }

    private KCallable sampleOwnerCallableOfType(KType type, Context ctx, boolean allowSubtypes) throws CloneNotSupportedException {
        // Sample callables that are either identifiers or constructors
        Predicate<KCallable> constructorOrIdOrAnon = kCallable -> kCallable instanceof KConstructor ||
                kCallable instanceof KIdentifierCallable ||
                kCallable instanceof KAnonymousCallable;

        KCallable sampledOwner;
        try {
            sampledOwner = ctx.randomCallableOfType(type, Collections.singletonList(constructorOrIdOrAnon), allowSubtypes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Cannot sample an owner of type: " + type);
        }

        List<KCallable> sampledOwnerInput = new ArrayList<>();

        for (KType inputType : sampledOwner.getInputTypes()) {
            sampledOwnerInput.add(sampleOwnerCallableOfType(inputType, ctx, allowSubtypes));
        }

        sampledOwner.call(ctx, null, sampledOwnerInput);
        return sampledOwner;
    }

    @Override
    public void invariant() {

    }
}
