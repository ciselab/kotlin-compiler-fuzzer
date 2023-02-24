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
    public boolean hasParameterizedType(KType type) {
        return false;
    }

    @Override
    public KType getTypeFromGeneric(KGenericType type, KClassifierType ownerType, List<KClassifierType> additionalVisibleTypes) {
        List<KType> parameterTypes = new ArrayList<>();

        for (KGenericType gen : type.getGenerics()) {
            parameterTypes.add(getTypeFromGeneric(gen, ownerType, additionalVisibleTypes));
        }

        List<String> additionalVisibleTypeNames = additionalVisibleTypes.stream().map(KClassifierType::name).toList();

        KClassifierType res;
        if (additionalVisibleTypeNames.contains(type.name())) {
           res = additionalVisibleTypes.get(additionalVisibleTypeNames.indexOf(type.name()));
        } else {
            res = (KClassifierType) getTypeByName(type.name());
        }

        if (res instanceof KClassType) {
            return new KClassType(res.name(), res.getGenerics(), parameterTypes, res.canBeInherited(), res.canBeInstantiated());
        } else {
            return new KInterfaceType(res.name(), res.getGenerics(), parameterTypes);
        }
    }

    private KType getGenericTypeByName(String name) {

    }

    public boolean isInstanceOfType(KType unfilledParameterizedType, KType filledParameterizedType) {
        if (!(unfilledParameterizedType instanceof KClassifierType t1
                && filledParameterizedType instanceof KClassifierType t2)) return false;

        List<KGenericType> symbolicGenerics = t1.getGenerics();

        if (!symbolicGenerics.stream().allMatch(KGenericType::isSymbolic)) {
            throw new IllegalArgumentException("Type " + unfilledParameterizedType + " has non-symbolic generics: " + symbolicGenerics);
        }

        List<KGenericType> instantiatedGenerics = t2.getGenerics();

        if (instantiatedGenerics.stream().anyMatch(KGenericType::isSymbolic)) {
            throw new IllegalArgumentException("Type " + filledParameterizedType + " has symbolic generics: " + symbolicGenerics);
        }

        if (symbolicGenerics.size() != instantiatedGenerics.size()) {
            return false;
        }

        for (int i = 0; i < symbolicGenerics.size(); i++) {
            KType genericUpperBound = symbolicGenerics.get(i).upperBound();

            if (genericUpperBound == null) {
                continue;
            }

            KType genericInstance = getTypeFromGeneric(instantiatedGenerics.get(i));

            if (!isSubtypeOf(genericInstance, genericUpperBound)) {
                return false;
            }
        }

        return true;
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
