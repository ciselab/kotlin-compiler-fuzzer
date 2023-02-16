package org.fuzzer.representations.types;

import org.fuzzer.utils.ConstrainedDAG;
import org.fuzzer.utils.RandomNumberGenerator;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

import static org.fuzzer.utils.StringUtilities.removeGeneric;

public class DAGTypeEnvironment implements TypeEnvironment, Serializable {

    private final ConstrainedDAG<KType> dag;

    private final RandomNumberGenerator rng;

    public DAGTypeEnvironment(RandomNumberGenerator rng, ConstrainedDAG<KType> dag) {
        this.rng = rng;
        this.dag = dag;
    }

    public DAGTypeEnvironment(RandomNumberGenerator rng) {
        this.rng = rng;
        dag = new ConstrainedDAG<>(
                (Predicate<KType> & Serializable) t -> t instanceof KClassifierType,
                (Predicate<Set<KType>> & Serializable) parents -> {
                    boolean allClassifiers = parents.stream().allMatch(t -> t instanceof KClassifierType);

                    if (!allClassifiers)
                        return false;

                    List<KType> classes = parents.stream().filter(t -> t instanceof KClassType).toList();
                    if (classes.isEmpty())
                        return true;

                    return classes.size() == 1;
                }
        );
    }

    @Override
    public void populateEnvironment() {

    }

    @Override
    public boolean hasType(KType type) {
        return dag.contains(type);
    }

    @Override
    public boolean isSubtypeOf(KType subtype, KType supertype) {
        return dag.hasAncestor(subtype, supertype);
    }

    @Override
    public Set<KType> subtypesOf(KType type) {
        return dag.allDescendants(type);
    }

    @Override
    public Set<KType> supertypesOf(KType type) {
        return dag.allAncestors(type);
    }

    @Override
    public void addType(KType parent, KType newType) {
        addType(new HashSet<>(Collections.singleton(parent)), newType);
    }

    @Override
    public void addType(Set<KType> parents, KType newType) {
        dag.addNode(newType, parents);
    }

    @Override
    public KType getTypeByName(String typeName) {
        List<KType> matchingTypes = dag.allEntries().stream()
                .filter(type -> type.name().equals(typeName))
                .toList();

        if (matchingTypes.isEmpty()) {
            throw new IllegalArgumentException("Could not find type named: " + typeName + ".");
        }

        if (matchingTypes.size() > 1) {
            throw new IllegalArgumentException("Multiple types names " + typeName + " found: " + matchingTypes + ".");
        }

        return matchingTypes.get(0);
    }

    // Shortcut for now
    public KType getRootTypeByName(String typeName) {
        String rootTypeName = removeGeneric(typeName);

        List<KType> matchingTypes = dag.allEntries().stream()
                .filter(type -> removeGeneric(type.name()).equals(rootTypeName))
                .toList();

        if (matchingTypes.isEmpty()) {
            throw new IllegalArgumentException("Could not find type named: " + typeName + ".");
        }

        if (matchingTypes.size() > 1) {
            throw new IllegalArgumentException("Multiple types names " + typeName + " found: " + matchingTypes + ".");
        }

        return matchingTypes.get(0);
    }

    @Override
    public KType randomType() {
        List<KType> typeList = dag.allNodes().stream().toList();
        return typeList.get(rng.fromUniformDiscrete(0, typeList.size() - 1));
    }
}
