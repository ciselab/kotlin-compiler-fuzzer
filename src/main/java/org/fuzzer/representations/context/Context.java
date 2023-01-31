package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.*;
import org.fuzzer.representations.types.KType;
import org.fuzzer.representations.types.TreeTypeEnvironment;
import org.fuzzer.representations.types.TypeEnvironment;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.*;
import java.util.function.Predicate;

public class Context {
    private final IdentifierStore idStore;

    private final Set<KCallable> callables;
    private final TypeEnvironment typeHierarchy;

    private final RandomNumberGenerator rng;

    public Context(RandomNumberGenerator rng) {
        this.callables = new HashSet<>();

        this.typeHierarchy = new TreeTypeEnvironment(rng);
        this.idStore = new MapIdentifierStore(typeHierarchy, rng);
        this.rng = rng;
    }

    public Boolean hasAnyVariables() {
        return !idStore.isEmpty();
    }

    public List<KCallable> identifiersOfType(KType type) {
        return idStore.callablesOfType(type);
    }

    public String randomIdentifier() {
        return idStore.randomIdentifier();
    }

    public KType typeOfIdentifier(String id) {
        return idStore.typeOfIdentifier(id);
    }

    public Boolean isSubtypeOf(KType subtype, KType supertype) {
        return typeHierarchy.isSubtypeOf(subtype, supertype);
    }

    public Optional<KCallable> randomCallableOfType(KType type, Predicate<KCallable> condition) throws CloneNotSupportedException {
        Set<KType> subtypes = typeHierarchy.subtypesOf(type);
        List<KCallable> alternatives = new ArrayList<>(callables
                .stream()
                .filter(kCallable -> subtypes.contains(kCallable.getReturnType()))
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
        return idStore.hasIdentifier(identifier);
    }

    public void addType(KType parent, KType newType) {
        typeHierarchy.addType(parent, newType);
    }

    public void addIdentifier(String id, KCallable callable) {
        idStore.addIdentifier(id, callable);
    }

    public KType getRandomType() {
        return typeHierarchy.randomType();
    }
}
