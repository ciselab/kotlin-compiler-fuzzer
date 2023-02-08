package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

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
