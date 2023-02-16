package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

public record KTypeWrapper(List<KTypeWrapper> parent,
                           KTypeIndicator indicator,
                           String name,
                           List<KGenericType> generics,
                           List<KTypeWrapper> inputTypes,
                           KTypeWrapper returnType) {

    public KTypeWrapper(KTypeIndicator indicator, String name) {
        this(new ArrayList<>(), indicator, name, new ArrayList<>(), new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public KTypeWrapper(KTypeIndicator indicator, String name, List<KGenericType> generics) {
        this(new ArrayList<>(), indicator, name, generics, new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public static KTypeWrapper getVoidWrapper() {
        return new KTypeWrapper(new ArrayList<>(), KTypeIndicator.CLASS, "void", new ArrayList<>(), new ArrayList<>(), null);
    }

    public KClassType toClass(boolean open, boolean abs) {
        return new KClassType(name, open, abs);
    }

    public KInterfaceType toInterface() {
        return new KInterfaceType(name);
    }

    public KFuncType toFunction(List<KType> inputTypes, KType returnType) {
        return new KFuncType(name, generics, inputTypes);
    }

    public KType toType(boolean open, boolean abs) {
        switch (indicator) {
            case CLASS -> {
                return toClass(open, abs);
            }
            case INTERFACE -> {
                return toInterface();
            }
            case FUNCTION -> {
                List<KType> inputTypes = inputTypes().stream()
                        .map(typeWrapper -> typeWrapper.toType(open, abs))
                        .toList();

                return toFunction(inputTypes, this.returnType.toType(open, abs));
            }
            case VOID -> {
                return new KVoid();
            }
            default -> {
                throw new IllegalArgumentException("Cannot handle indicator of type: " + indicator);
            }
        }
    }

    public KType toType() {
        return toType(false, false);
    }

    public KType toType(KTypeModifiers modifiers) {
        return toType(modifiers.isOpen(), modifiers.isAbstract());
    }
}
