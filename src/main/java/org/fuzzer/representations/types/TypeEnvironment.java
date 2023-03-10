package org.fuzzer.representations.types;

import org.jetbrains.kotlin.spec.grammar.tools.KotlinParseTree;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface TypeEnvironment extends Serializable {
    void populateEnvironment();

    boolean hasType(KType type);

    boolean hasParameterizedType(KType type);

    KType getTypeFromGeneric(KGenericType type, KClassifierType ownerType, List<KGenericType> additionalVisibleTypes);

    boolean containsType(KClassifierType type);

    boolean containsGenericType(KGenericType generic, KClassifierType owner);

    boolean isSubtypeOf(KType subtype, KType supertype);

    Set<KType> subtypesOf(KType type);

    Set<KType> supertypesOf(KType type);

    void addType(KType parent, KType newType);

    void addType(Set<KType> parents, KType newType);

    KType getTypeByName(String typeName);

    KType getRootTypeByName(String typeName);

    KType randomType();
}
