package org.fuzzer.representations.types;

import java.util.Objects;
import java.util.Optional;

public class KType {
    private final String name;

    private final Optional<KType> genericType;

    public KType(String name) {
        this.name = name;
        this.genericType = Optional.empty();
    }

    public KType(String name, KType genericType) {
        this.name = name;
        this.genericType = Optional.of(genericType);
    }

    public static KType getNull() {
        return new KType("null");
    }

    public String getName() {
        return name;
    }

    public Optional<KType> getGenericType() {
        return genericType;
    }

    public Boolean hasGeneric() {
        return !genericType.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KType that)) return false;

        return Objects.equals(name, that.name) && Objects.equals(this.genericType, that.genericType);
    }

    @Override
    public String toString() {
        return "[" + name + (genericType.isPresent() ? ("<" + genericType.get() + ">") : "") + "]";
    }
}
