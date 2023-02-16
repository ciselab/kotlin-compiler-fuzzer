package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.StringUtilities;
import org.fuzzer.utils.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.fuzzer.utils.StringUtilities.removeGeneric;

public class AssignmentNode extends ASTNode {

    private final int maxDepth;
    public AssignmentNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new ArrayList<>());
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        KType type;
        String id;
        boolean sampleExisting = ctx.hasAnyVariables() && rng.randomBoolean();

        if (sampleExisting) {
            id = ctx.randomIdentifier();
            type = ctx.typeOfIdentifier(id);
        } else {
            id = StringUtilities.randomIdentifier();
            while (ctx.containsIdentifier(id)) {
                id = StringUtilities.randomIdentifier();
            }
            type = ctx.getRandomType();

            // Hack for demonstration purposes
            while (removeGeneric(type.name()).equals("Comparable")) {
                type = ctx.getRandomType();
            }
        }

        int depth = 0;

        try {

            KCallable baseCallable = getCallableOfType(type, depth++, ctx, rng);
            Tree<KCallable> rootNode = new Tree<>(baseCallable);

            rootNode = sampleTypedCallables(rootNode, depth, ctx, rng);
            verifyCallableCompatibility(rootNode, ctx);

            String rhs = rootNode.getValue().call(ctx);
            String lhs = sampleExisting ? id : ("var " + id + ": " + type.name());

            if (!sampleExisting) {
                ctx.addIdentifier(id, new KIdentifierCallable(id, type));
            }

            return new CodeFragment(lhs + " = " + rhs);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
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
        Optional<KCallable> callable;

        if (sampleConsumerCallable) {
            callable = ctx.randomConsumerCallable(type);
            if (callable.isPresent()) {
                return callable.get();
            }
        }

        callable = ctx.randomTerminalCallableOfType(type);
        if (callable.isPresent()) {
            return callable.get();
        }

        if (depth >= maxDepth) {
            throw new IllegalStateException("Max depth exceeded, but no terminal found.");
        }

        // Rolled false, and no terminal callable found.
        callable = ctx.randomConsumerCallable(type);
        if (callable.isPresent()) {
            return callable.get();
        }

        throw new IllegalStateException("No callable found.");
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
            callable.call(ctx, Optional.of(owner), childrenCallables);
        } else {
            callable.call(ctx, Optional.empty(), childrenCallables);
        }


    }

    private KCallable sampleOwnerCallableOfType(KType type, Context ctx) throws CloneNotSupportedException {
        // Sample callables that are either identifiers or constructors
        Predicate<KCallable> constructorOrIdOrAnon = kCallable -> kCallable instanceof KConstructor ||
                kCallable instanceof KIdentifierCallable ||
                kCallable instanceof KAnonymousCallable;
        Optional<KCallable> sampledOwner = ctx.randomCallableOfType(type, constructorOrIdOrAnon);

        if (sampledOwner.isEmpty()) {
            throw new RuntimeException("Cannot sample an owner of type: " + type);
        }

        KCallable owner = sampledOwner.get();

        List<KCallable> sampledOwnerInput = new ArrayList<>();

        for (KType inputType : sampledOwner.get().getInputTypes()) {
            sampledOwnerInput.add(sampleOwnerCallableOfType(inputType, ctx));
        }

        owner.call(ctx, Optional.empty(), sampledOwnerInput);
        return owner;
    }

    @Override
    public void invariant() {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Parameter node with children.");
        }
    }
}
