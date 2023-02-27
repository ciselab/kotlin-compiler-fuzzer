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
    public boolean containsType(KClassifierType type) {
        if (type.getGenerics().isEmpty()) {
            return hasType(type);
        }

        boolean containsRoot = false;

        try {
            getTypeByName(type.name());
            containsRoot = true;
        } catch (IllegalArgumentException ignored) {}

        return containsRoot && type.getGenerics().stream().allMatch(g -> containsGenericType(g, type));
    }

    @Override
    public boolean containsGenericType(KGenericType generic, KClassifierType owner) {
        try {
            getTypeFromGeneric(generic, owner, owner.getGenerics());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Types extracted from generics can be one of three things:
     * (1) A symbolic type encountered earlier in the signature: Class<T> : SuperClass<T>
     * (2) A concrete type already present in the context: Class<X> : SuperClass<Int>
     * (3) A type (partly) dependent on a type that is not yet in the context: Double : Comparable<Double>
     * Types are recursively unrolled untill non-parameterized types are reached
     * @param type The symbolic representation of a generic type
     * @param ownerType The newly delcared (possibly parameterized) type
     * @param additionalVisibleTypes Symbolic types that parameterize the ownerType
     * @return The type that the generic refers to (one of the three cases above), if one exists.
     */
    @Override
    public KType getTypeFromGeneric(KGenericType type, KClassifierType ownerType, List<KGenericType> additionalVisibleTypes) {
        List<KType> parameterTypes = new ArrayList<>();

        for (KGenericType gen : type.getGenerics()) {
            parameterTypes.add(getTypeFromGeneric(gen, ownerType, additionalVisibleTypes));
        }

        List<String> additionalVisibleTypeNames = additionalVisibleTypes.stream().map(KGenericType::name).toList();

        KType res;

        // If the symbolic generics contain this type, use it
        if (additionalVisibleTypeNames.contains(type.name())) {
           res = additionalVisibleTypes.get(additionalVisibleTypeNames.indexOf(type.name()));
        } else {

            // If the type is the same as the base declared type
            if (ownerType.name().equals(type.name())) {
                res = ownerType;
            } else {

                // If neither the symbolics nor the base type match, the type must be in the environment
                res = (KClassifierType) getTypeByName(type.name());
            }
        }

        if (res instanceof KClassType) {
            return new KClassType(res.name(), res.getGenerics(), parameterTypes, res.canBeInherited(), res.canBeInstantiated());
        }

        if (res instanceof KInterfaceType) {
            return new KInterfaceType(res.name(), res.getGenerics(), parameterTypes);
        }

        if (res instanceof KGenericType) {
            return res;
        }

        throw new IllegalStateException("Generic resolved to type: " + res);
    }

//    public boolean isInstanceOfType(KType unfilledParameterizedType, KType filledParameterizedType) {
//        if (!(unfilledParameterizedType instanceof KClassifierType t1
//                && filledParameterizedType instanceof KClassifierType t2)) return false;
//
//        List<KGenericType> symbolicGenerics = t1.getGenerics();
//
//        if (!symbolicGenerics.stream().allMatch(KGenericType::isSymbolic)) {
//            throw new IllegalArgumentException("Type " + unfilledParameterizedType + " has non-symbolic generics: " + symbolicGenerics);
//        }
//
//        List<KGenericType> instantiatedGenerics = t2.getGenerics();
//
//        if (instantiatedGenerics.stream().anyMatch(KGenericType::isSymbolic)) {
//            throw new IllegalArgumentException("Type " + filledParameterizedType + " has symbolic generics: " + symbolicGenerics);
//        }
//
//        if (symbolicGenerics.size() != instantiatedGenerics.size()) {
//            return false;
//        }
//
//        for (int i = 0; i < symbolicGenerics.size(); i++) {
//            KType genericUpperBound = symbolicGenerics.get(i).upperBound();
//
//            if (genericUpperBound == null) {
//                continue;
//            }
//
//            KType genericInstance = getTypeFromGeneric(instantiatedGenerics.get(i), unfilledParameterizedType, unfilledParameterizedType.getGenerics());
//
//            if (!isSubtypeOf(genericInstance, genericUpperBound)) {
//                return false;
//            }
//        }
//
//        return true;
//    }


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
