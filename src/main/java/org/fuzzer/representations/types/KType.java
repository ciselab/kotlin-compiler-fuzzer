package org.fuzzer.representations.types;

import java.io.Serializable;
import java.util.List;

public interface KType extends Serializable {

    String name();

    List<KGenericType> getGenerics();

    public List<KType> getInputTypes();

    public KType getReturnType();

    boolean canBeInherited();

    boolean canBeInstantiated();

    boolean canBeDeclared();
}
