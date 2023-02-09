package org.fuzzer.representations.types;

import java.util.List;
import java.util.Optional;

public interface KType {

    String name();

    List<KGenericType> getGenerics();

    public List<KType> getInputTypes();

    public Optional<KType> getReturnType();

    boolean canBeInherited();

    boolean canBeInstantiated();

    boolean canBeDeclared();
}
