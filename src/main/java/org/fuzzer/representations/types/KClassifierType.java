package org.fuzzer.representations.types;

import kotlin.contracts.Returns;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class KClassifierType implements KType {
    private final String name;

    private final List<KGenericType> generics;

    protected KClassifierType(String name, List<KGenericType> generics) {
        this.name = name;
        this.generics = generics;
    }

    public KClassifierType(String name) {
        this.name = name;
        this.generics = new ArrayList<>();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<KType> getInputTypes() {
        return new ArrayList<>();
    }

    public Optional<KType> getReturnType() {
        return Optional.empty();
    }

    @Override
    public List<KGenericType> getGenerics() {
        return generics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KClassifierType that)) return false;

        if (!name.equals(that.name)) return false;
        return generics.equals(that.generics);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + generics.hashCode();
        return result;
    }
}
