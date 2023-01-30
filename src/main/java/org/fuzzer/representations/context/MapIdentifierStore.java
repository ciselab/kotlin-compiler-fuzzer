package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KType;
import org.fuzzer.representations.types.TypeEnvironment;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class MapIdentifierStore implements IdentifierStore {

    private final Map<String, KCallable> identifierMap;
    private final TypeEnvironment typeEnvironment;

    private final RandomNumberGenerator rng;

    public MapIdentifierStore(TypeEnvironment typeEnvironment, RandomNumberGenerator rng) {
        this.identifierMap = new HashMap<>();
        this.typeEnvironment = typeEnvironment;
        this.rng = rng;
    }

    @Override
    public boolean isEmpty() {
        return identifierMap.isEmpty();
    }

    @Override
    public List<KCallable> identifiersOfType(KType type) {
        Set<KType> allSubtypes = typeEnvironment.subtypesOf(type);
        return identifierMap.entrySet()
                .stream()
                .filter(entry -> allSubtypes.contains(entry.getValue().getOutputType()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasIdentifier(String identifier) {
        return identifierMap.containsKey(identifier);
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
    public void addIdentifier(String identifier, KCallable callable) {
        verifyNotExists(identifier);
        identifierMap.put(identifier, callable);
    }

    @Override
    public void updateIdentifier(String identifier, KCallable callable) {
        verifyNotExists(identifier);
        identifierMap.put(identifier, callable);
    }

    @Override
    public KType typeOfIdentifier(String identifier) {
        verifyExists(identifier);
        return identifierMap.get(identifier).getOutputType();
    }

    @Override
    public List<KCallable> callablesOfType(KType returnType) {
        Set<KType> subtypes = typeEnvironment.subtypesOf(returnType);
        return new ArrayList<>(identifierMap
                .entrySet()
                .stream()
                .filter(entry -> subtypes.contains(entry.getValue().getOutputType()))
                .map(Map.Entry::getValue)
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
