package org.fuzzer.grammar;

import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.types.KType;
import org.fuzzer.representations.types.TreeTypeEnvironment;
import org.fuzzer.representations.types.TypeEnvironment;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.StringUtilities;
import org.fuzzer.utils.Tree;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Context {
    private final Map<String, KCallable> identifiers;

    private final Set<KCallable> callables;
    private final TypeEnvironment typeHierarchy;

    private final RandomNumberGenerator rng;

    public Context(RandomNumberGenerator rng) {
        this.identifiers = new HashMap<>();
        this.callables = primitiveConsumerCallables();
        callables.addAll(primitiveTerminalCallables(rng));

        this.typeHierarchy = new TreeTypeEnvironment(rng);
        this.rng = rng;
    }

    private static Set<KCallable> primitiveConsumerCallables() {
        Set<KCallable> numericCallables =
                Arrays.stream(new String[]{"+", "-", "*", "/"})
                        .map(KPrimitiveNumericBinOp::new).collect(Collectors.toSet());

        Set<KCallable> logicCallables =
                Arrays.stream(new String[]{"&&", "||"})
                        .map(KPrimitiveLogicBinOp::new).collect(Collectors.toSet());

        numericCallables.addAll(logicCallables);

        return numericCallables;
    }

    private static Set<KCallable> primitiveTerminalCallables(RandomNumberGenerator rng) {
        Set<KCallable> res = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            res.add(new KAnonymousCallable(new KType("Int"), rng.randomNumberPrimitive().toString()));
            res.add(new KAnonymousCallable(new KType("Long"), rng.randomNumberPrimitive().toString()));
            res.add(new KAnonymousCallable(new KType("Short"), rng.randomNumberPrimitive().toString()));
            res.add(new KAnonymousCallable(new KType("Byte"), rng.randomByte().toString()));
            res.add(new KAnonymousCallable(new KType("String"), StringUtilities.randomString()));
            res.add(new KAnonymousCallable(new KType("Char"), StringUtilities.randomChar()));

        }

        res.add(new KAnonymousCallable(new KType("Boolean"), Boolean.FALSE.toString()));
        res.add(new KAnonymousCallable(new KType("Boolean"), Boolean.TRUE.toString()));

        return res;
    }

    public Boolean hasAnyVariables() {
        return !identifiers.isEmpty();
    }

    public List<KCallable> identifiersOfType(KType type) {
        Set<KType> subtypes = typeHierarchy.supertypesOf(type);
        List<KCallable> alternatives = identifiers
                .entrySet()
                .stream()
                .filter(tuple ->  subtypes.contains(tuple.getValue().getOutputType()))
                .map(Map.Entry::getValue)
                .toList();

        return alternatives;
    }

    public String randomIdentifier() {
        if (!hasAnyVariables())
            throw new IllegalStateException("Cannot sample identifiers from empty context.");
        List<String> allIdentifiers = identifiers.keySet().stream().toList();
        return allIdentifiers.get(rng.fromUniformDiscrete(0, allIdentifiers.size() - 1));
    }

    public KType typeOfIdentifier(String id) {
        if (!identifiers.containsKey(id))
            throw new IllegalArgumentException("Identifier " + id + " not defined in context.");

        return identifiers.get(id).getOutputType();
    }

    public Boolean isSubtypeOf(KType subtype, KType supertype) {
        return typeHierarchy.isSubtypeOf(subtype, supertype);
    }

    public Optional<KCallable> randomCallableOfType(KType type, Predicate<KCallable> condition) throws CloneNotSupportedException {
        Set<KType> subtypes = typeHierarchy.subtypesOf(type);
        List<KCallable> alternatives = new ArrayList<>(callables
                .stream()
                .filter(kCallable -> subtypes.contains(kCallable.getOutputType()))
                .filter(condition)
                .toList());

        alternatives.addAll(identifiersOfType(type));

        if (alternatives.isEmpty())
            return Optional.empty();
        KCallable selected = alternatives.get(rng.fromUniformDiscrete(0, alternatives.size() - 1));
        return Optional.of((KCallable) selected.clone());
    }

    public Optional<KCallable> randomTerminalCallableOfType(KType type) throws CloneNotSupportedException {
        return randomCallableOfType(type, kCallable -> kCallable.getInputTypes().isEmpty());
    }

    public Optional<KCallable> randomConsumerCallable(KType type) throws CloneNotSupportedException {
        return randomCallableOfType(type, kCallable -> !kCallable.getInputTypes().isEmpty());
    }

    public boolean containsIdentifier(String identifier) {
        return identifiers.containsKey(identifier);
    }

    public void addType(KType parent, KType newType) {
        typeHierarchy.addType(parent, newType);
    }

    public void addIdentifier(String id, KIdentifierCallable callable) {
        if (identifiers.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already defined in context.");

        identifiers.put(id, callable);
    }

    public KType getRandomType() {
        return typeHierarchy.randomType();
    }
}
