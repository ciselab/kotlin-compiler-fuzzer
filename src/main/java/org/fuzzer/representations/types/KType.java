package org.fuzzer.representations.types;

import java.util.List;

public interface KType {

    String getName();

    List<KType> getGenerics();

    boolean canBeInherited();

    boolean canBeInstantiated();

    boolean canBeDeclared();
}
