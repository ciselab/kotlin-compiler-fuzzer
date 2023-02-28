package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record KGenericType(String name, KTypeIndicator genericKind, KGenericType upperBound) implements KType {

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

    public boolean isSymbolic() {
        if (!(KTypeIndicator.SYMBOLIC_GENERIC.equals(genericKind) ||
                KTypeIndicator.CONCRETE_GENERIC.equals(genericKind))) {
            throw new IllegalStateException("Ill-formed generic type of kind: " + genericKind);
        }

        return genericKind.equals(KTypeIndicator.SYMBOLIC_GENERIC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KGenericType that)) return false;

        if (genericKind != that.genericKind) return false;

        // Important: any two symbolic generic types are "equal".
        // Concrete generics are only equal if they have the same names.
        switch (genericKind) {
            case SYMBOLIC_GENERIC -> {
                return true;
            }
            case CONCRETE_GENERIC -> {
                return this.name.equals(that.name);
            }
            default -> throw new IllegalStateException("Generic kind of type " + genericKind + "encountered.");
        }
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (genericKind != null ? genericKind.hashCode() : 0);
        return result;
    }
}
