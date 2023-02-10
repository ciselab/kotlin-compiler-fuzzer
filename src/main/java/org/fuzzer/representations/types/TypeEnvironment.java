package org.fuzzer.representations.types;

import org.jetbrains.kotlin.spec.grammar.tools.KotlinParseTree;

import java.util.Set;

public interface TypeEnvironment {
    void populateEnvironment();

    boolean hasType(KType type);

    boolean isSubtypeOf(KType subtype, KType supertype);

    Set<KType> subtypesOf(KType type);

    Set<KType> supertypesOf(KType type);

    void addType(KType parent, KType newType);

    void addType(Set<KType> parents, KType newType);

    KType getTypeByName(String typeName);

    KType getRootTypeByName(String typeName);
    KType randomType();
}
