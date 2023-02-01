package org.fuzzer.representations.types;

import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tree;

import java.util.*;
import java.util.stream.Collectors;

public class TreeTypeEnvironment implements TypeEnvironment {
    private Tree<KType> typeTree;

    private List<KType> listOfTypes;

    private final RandomNumberGenerator rng;

    public TreeTypeEnvironment(RandomNumberGenerator rng) {
        this.rng = rng;
        populateEnvironment();
    }

    public TreeTypeEnvironment(Tree<KType> typeTree, RandomNumberGenerator rng) {
        this.typeTree = typeTree;
        this.rng = rng;
        this.listOfTypes = typeTree.toList();
    }

    /**
     * TODO Populate from Kotlin primitives and stdlib
     */
    @Override
    public void populateEnvironment() {
        return;
    }

    @Override
    public boolean hasType(KType type) {
        return listOfTypes.contains(type);
    }

    @Override
    public boolean isSubtypeOf(KType subtype, KType supertype) {
        checkExistence(subtype);
        checkExistence(supertype);

        return typeTree.find(supertype).get().hasDescendant(subtype);
    }

    @Override
    public Set<KType> subtypesOf(KType type) {
        checkExistence(type);
        return typeTree.find(type).get().allDescendants()
                .stream()
                .map(Tree<KType>::getValue)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<KType> supertypesOf(KType type) {
        checkExistence(type);

        Set<KType> allSupertypes = new HashSet<>();
        allSupertypes.add(type);

        Tree<KType> currentNode = typeTree.find(type).get();

        while (currentNode.getParent().isPresent()) {
            currentNode = currentNode.getParent().get();
            allSupertypes.add(currentNode.getValue());
        }

        return allSupertypes;
    }

    @Override
    public void addType(KType parent, KType newType) {
        checkExistence(parent);
        if (hasType(newType)) {
            throw new IllegalArgumentException("Type " + newType + " already exists in type hierarchy.");
        }

        typeTree.find(parent).get().addChild(newType);
        listOfTypes.add(newType);
    }

    @Override
    public KType randomType() {
        return listOfTypes.get(rng.fromUniformDiscrete(0, listOfTypes.size() - 1));
    }

    private void checkExistence(KType type) {
        if (!hasType(type))
            throw new IllegalArgumentException("Type " + type + " not available in type hierarchy.");
    }
}
