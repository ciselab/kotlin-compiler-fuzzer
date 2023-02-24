package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

public record KTypeWrapper(KTypeModifiers modifiers,
                           KTypeWrapper upperBound,
                           List<KTypeWrapper> parent,
                           KTypeIndicator indicator,
                           String name,
                           List<KTypeWrapper> generics,
                           List<KTypeWrapper> inputTypes,
                           KTypeWrapper returnType) {

    public KTypeWrapper(KTypeWrapper upperBound, KTypeIndicator indicator, String name) {
        this(null, upperBound, new ArrayList<>(), indicator, name, new ArrayList<>(), new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public KTypeWrapper(KTypeIndicator indicator, String name) {
        this(null, null, new ArrayList<>(), indicator, name, new ArrayList<>(), new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public KTypeWrapper(KTypeIndicator indicator, String name, List<KTypeWrapper> generics) {
        this(null, null, new ArrayList<>(), indicator, name, generics, new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public static KTypeWrapper getVoidWrapper() {
        return new KTypeWrapper(null, null, new ArrayList<>(), KTypeIndicator.CLASS, "void", new ArrayList<>(), new ArrayList<>(), null);
    }

    public KClassType toClass(boolean open, boolean abs) {
        return new KClassType(name, open, abs);
    }

    public KInterfaceType toInterface() {
        return new KInterfaceType(name);
    }

    public KFuncType toFunction(List<KType> inputTypes, KType returnType) {
        return new KFuncType(name, generics.stream().map(wrapper -> (KGenericType) wrapper.toType()).toList(), inputTypes);
    }

    public boolean isOpen() {
        if (modifiers != null) {
            return modifiers.isOpen();
        }

        return true;
    }

    public boolean isAbstract() {
        if (modifiers != null) {
            return modifiers.isAbstract();
        }

        return false;
    }

    public KType toType() {
        switch (indicator) {
            case CLASS -> {
                List<KGenericType> genericTypes = generics.stream().map(wrapper -> (KGenericType) wrapper.toType()).toList();
                return new KClassType(name, genericTypes, isOpen(), isAbstract());
            }
            case INTERFACE -> {
                List<KGenericType> genericTypes = generics.stream().map(wrapper -> (KGenericType) wrapper.toType()).toList();
                return new KInterfaceType(name, genericTypes);
            }
            case FUNCTION -> {
                List<KType> inputTypes = inputTypes().stream()
                        .map(KTypeWrapper::toType)
                        .toList();

                return toFunction(inputTypes, this.returnType.toType());
            }
            case VOID -> {
                return new KVoid();
            }
            case CONCRETE_GENERIC -> {
                KGenericType upperType = new KGenericType(upperBound.name, KTypeIndicator.CONCRETE_GENERIC, null);
                return new KGenericType(name, KTypeIndicator.CONCRETE_GENERIC, upperType);
            }
            case SYMBOLIC_GENERIC -> {
                KGenericType upperType = new KGenericType(upperBound.name, KTypeIndicator.CONCRETE_GENERIC, null);
                return new KGenericType(name, KTypeIndicator.SYMBOLIC_GENERIC, upperType);
            }
            default -> {
                throw new IllegalArgumentException("Cannot handle indicator of type: " + indicator);
            }
        }
    }
}
