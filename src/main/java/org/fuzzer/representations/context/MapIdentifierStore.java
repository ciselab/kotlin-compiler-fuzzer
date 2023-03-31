package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.callables.KFunction;
import org.fuzzer.representations.callables.KIdentifierCallable;
import org.fuzzer.representations.callables.KMethod;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.representations.types.TypeEnvironment;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.*;

public class MapIdentifierStore implements IdentifierStore {

    private final Map<String, KCallable> identifierMap;

    private final Map<String, KType> typeMap;
    private final TypeEnvironment typeEnvironment;

    private RandomNumberGenerator rng;

    public MapIdentifierStore(TypeEnvironment typeEnvironment, RandomNumberGenerator rng) {
        this.identifierMap = new HashMap<>();
        this.typeMap = new HashMap<>();
        this.typeEnvironment = typeEnvironment;
        this.rng = rng;
    }

    @Override
    public boolean isEmpty() {
        return identifierMap.isEmpty();
    }

    @Override
    public boolean hasAssignableIdentifiers() {
        return !allAssignableIdentifiers().isEmpty();
    }

    @Override
    public List<KCallable> allIdentifiers() {
        return identifierMap.values().stream().toList();
    }

    @Override
    public List<KCallable> allAssignableIdentifiers() {
        return allIdentifiers().stream()
                .filter(this::isMutable)
                .toList();
    }

    /**
     * Whether a callable is mutable.
     */
    private boolean isMutable(KCallable callable) {
        if (! (callable instanceof KIdentifierCallable identifier)) {
            return  false;
        }

        return identifier.isMutable();
    }

    @Override
    public String randomAssignableIdentifier() {
        List<KCallable> alternatives = allAssignableIdentifiers();
        List<String> idNames = alternatives.stream().map(KCallable::getName).toList();
        return idNames.get(rng.fromUniformDiscrete(0, idNames.size() - 1));
    }

    @Override
    public List<KCallable> identifiersOfType(KType type) {
        Set<KType> allSubtypes = typeEnvironment.subtypesOf(type);
        return typeMap.entrySet()
                .stream()
                .filter(entry -> allSubtypes.contains(entry.getValue()))
                .map(entry -> identifierMap.get(entry.getKey()))
                .toList();
    }

    @Override
    public boolean hasIdentifier(String identifier) {
        return identifierMap.containsKey(identifier);
    }

    @Override
    public KCallable getIdentifier(String identifier) {
        verifyExists(identifier);
        return identifierMap.get(identifier);
    }

    @Override
    public String randomIdentifier() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot sample identifiers from empty id store.");
        }

        List<String> identifierList = identifierMap.keySet().stream().toList();
        return identifierList.get(rng.fromUniformDiscrete(0, identifierList.size() - 1));
    }

    @Override
    public void updateRNG(RandomNumberGenerator rng) {
        this.rng = rng;
    }

    @Override
    public void addIdentifier(String identifier, KCallable callable) {
        verifyNotExists(identifier);
        identifierMap.put(identifier, callable);
        typeMap.put(identifier, callable.getReturnType());
    }

    @Override
    public void updateIdentifier(String identifier, KCallable callable) {
        verifyExists(identifier);

        KType currentType = getIdentifier(identifier).getReturnType();
        if (!typeEnvironment.isSubtypeOf(callable.getReturnType(), currentType))
            throw new IllegalArgumentException("Identifier " + identifier + " of type " + currentType + " cannot be assigned to type " + callable.getReturnType());

        identifierMap.put(identifier, callable);
    }

    @Override
    public KType typeOfIdentifier(String identifier) {
        verifyExists(identifier);
        return typeMap.get(identifier);
    }

    @Override
    public List<KCallable> callablesOfType(KType returnType) {
        Set<KType> subtypes = typeEnvironment.subtypesOf(returnType);
        return new ArrayList<>(typeMap
                .entrySet()
                .stream()
                .filter(entry -> subtypes.contains(entry.getValue()))
                .map(entry -> identifierMap.get(entry.getKey()))
                .toList());
    }

    private void verifyExists(String identifier) {
        if (!hasIdentifier(identifier))
            throw new IllegalArgumentException("Identifier " + identifier + " already exists in store.");
    }

    private void verifyNotExists(String identifier) {
        if (hasIdentifier(identifier))
            throw new IllegalArgumentException("Identifier " + identifier + " already exists in store.");
    }
}
