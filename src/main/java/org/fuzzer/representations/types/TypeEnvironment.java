package org.fuzzer.representations.types;

import java.util.Set;

public interface TypeEnvironment {
    void populateEnvironment();

    boolean hasType(KType type);

    boolean isSubtypeOf(KType subtype, KType supertype);

    Set<KType> subtypesOf(KType type);

    Set<KType> supertypesOf(KType type);

    void addType(KType parent, KType newType);

    KType randomType();
}
