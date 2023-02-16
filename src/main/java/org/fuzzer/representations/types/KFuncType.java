package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

public class KFuncType implements KType {

    private final String name;

    private final List<KGenericType> generics;

    private final List<KType> inputTypes;

    private final KType returnType;

    public KFuncType(String name, List<KGenericType> generics, List<KType> inputTypes, KType outputType) {
        this.name = name;
        this.generics = generics;
        this.inputTypes = inputTypes;
        this.returnType = outputType;
    }

    public KFuncType(String name, List<KGenericType> generics, List<KType> inputTypes) {
        this.name = name;
        this.generics = generics;
        this.inputTypes = inputTypes;
        this.returnType = new KVoid();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<KType> getInputTypes() {
        return inputTypes;
    }

    @Override
    public KType getReturnType() {
        return returnType;
    }

    @Override
    public List<KGenericType> getGenerics() {
        return new ArrayList<>();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KFuncType kFuncType)) return false;

        if (!name.equals(kFuncType.name)) return false;
        if (!generics.equals(kFuncType.generics)) return false;
        if (!inputTypes.equals(kFuncType.inputTypes)) return false;
        return returnType.equals(kFuncType.returnType);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + generics.hashCode();
        result = 31 * result + inputTypes.hashCode();
        result = 31 * result + returnType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "[func]: " + name() + inputTypes + "->" + returnType;
    }
}
