package org.fuzzer.grammar.ast.expressions;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KFuncType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tree;
import org.fuzzer.utils.Tuple;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExpressionNode extends ASTNode {

    protected final int maxDepth;

    public ExpressionNode(GrammarAST antlrNode, int maxDepth) {
        super(antlrNode, new ArrayList<>());
        this.maxDepth = maxDepth;
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        return getRandomExpressionNode(rng).getSample(rng, ctx, generatedCallableDependencies);
    }

    public ExpressionNode getRandomExpressionNode(RandomNumberGenerator rng) {
        if (rng.fromUniformContinuous(0.0, 1.0) < cfg.getSimplicityBias()) {
            return createExpressionNodeFromStructure(SampleStructure.SIMPLE_EXPR);
        } else {
            SampleStructure selectedStructure = rng.fromProbabilityTable(cfg.getExpressionProbabilityTable());
            return createExpressionNodeFromStructure(selectedStructure);
        }
    }

    private ExpressionNode createExpressionNodeFromStructure(SampleStructure structure) {
        ExpressionNode node;

        switch (structure) {
            case IF_EXPR -> node = new IfExpressionNode(antlrNode, maxDepth);
            case SIMPLE_EXPR -> node = new SimpleExpressionNode(antlrNode, maxDepth);
            case TRY_CATCH -> node = new TryExpressionNode(antlrNode, maxDepth);
            case ELVIS_OP -> node = new ElvisOpExpression(antlrNode, maxDepth);
            default ->
                throw new IllegalArgumentException("Cannot create expression node of structure: " + structure);
        }

        node.useConfiguration(cfg);
        node.recordStatistics(stats);

        return node;
    }

    public Tuple<CodeFragment, Tuple<KType, List<KType>>> getSampleOfType(RandomNumberGenerator rng, Context ctx, KType type,
                                                                          boolean allowSubtypes, Set<String> generatedCallableDependencies) {
        try {
            int depth = 0;

            KCallable baseCallable = getCallableOfType(type, depth++, ctx, rng);
            Tree<KCallable> rootNode = new Tree<>(baseCallable);

            KType returnType = baseCallable.getReturnType();

            List<KType> typeParameterInstances = ctx.getParameterInstances(type, returnType);

            if (typeParameterInstances.size() != type.getGenerics().size()) {
                throw new IllegalStateException("Sampling subtypes failed");
            }

            rootNode = sampleTypedCallables(rootNode, depth, ctx, rng);
            verifyCallableCompatibility(rootNode, ctx, allowSubtypes);
            String expression = rootNode.getValue().call(ctx);

            CodeFragment code = new CodeFragment(expression);

            if (stats != null) {
                stats.increment(SampleStructure.SIMPLE_EXPR);
            }

            generatedCallableDependencies.addAll(getGeneratedCallableNames(rootNode));

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

    private Set<String> getGeneratedCallableNames(Tree<KCallable> callableTree) {
        return callableTree.toList().stream().filter(KCallable::isGenerated).map(KCallable::getName).collect(Collectors.toSet());
    }

    private KCallable sampleOwnerCallableOfType(KType type, Context ctx, boolean allowSubtypes) throws CloneNotSupportedException {
        // Sample callables that are either identifiers or constructors
        Predicate<KCallable> constructorOrIdOrAnon = kCallable -> kCallable instanceof KConstructor ||
                kCallable instanceof KIdentifierCallable ||
                kCallable instanceof KAnonymousCallable;
        Predicate<KCallable> noFunctionInputs = kCallable -> kCallable.getInputTypes().stream().noneMatch(input -> input instanceof KFuncType);
        Predicate<KCallable> onlySamplableInputTypes = kCallable -> {
            return new HashSet<>(ctx.samplableTypes()).containsAll(kCallable.getInputTypes());
        };

        List<Predicate<KCallable>> predicates = new LinkedList<>();
        predicates.add(constructorOrIdOrAnon);
        predicates.add(noFunctionInputs);
        predicates.add(onlySamplableInputTypes);

        KCallable sampledOwner;
        try {
            sampledOwner = ctx.randomCallableOfType(type, predicates, allowSubtypes);
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
