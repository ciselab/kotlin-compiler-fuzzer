package org.fuzzer.representations.types;

import java.util.List;

public interface KType {

    String name();

    List<KGenericType> getGenerics();

    boolean canBeInherited();

    boolean canBeInstantiated();

    boolean canBeDeclared();
}
