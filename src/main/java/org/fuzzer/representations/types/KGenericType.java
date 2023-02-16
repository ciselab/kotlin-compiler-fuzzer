package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

public record KGenericType(String name) implements KType {

    @Override
    public List<KGenericType> getGenerics() {
        return new ArrayList<>();
    }

    @Override
    public List<KType> getInputTypes() {
        return new ArrayList<>();
    }

    @Override
    public KType getReturnType() {
        return new KVoid();
    }

    @Override
    public boolean canBeInherited() {
        return false;
    }

    @Override
    public boolean canBeInstantiated() {
        return false;
    }

    @Override
    public boolean canBeDeclared() {
        return false;
    }
}
