package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

public record KGenericType(String name, KTypeIndicator genericKind) implements KType {

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
}
