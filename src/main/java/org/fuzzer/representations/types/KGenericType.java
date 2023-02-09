package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<KType> getReturnType() {
        return Optional.empty();
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
