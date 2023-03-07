package org.fuzzer.representations.types;

import org.fuzzer.representations.callables.*;
import org.fuzzer.utils.KGrammarVocabulary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public record KTypeWrapper(KTypeWrapper ownerType,
                           String varName,
                           KTypeModifiers modifiers,
                           KTypeWrapper upperBound,
                           List<KTypeWrapper> parent,
                           KTypeIndicator indicator,
                           String name,
                           List<KTypeWrapper> generics,
                           List<KTypeWrapper> inputTypes,
                           KTypeWrapper returnType) {

    public KTypeWrapper(String varName, KTypeWrapper wrapper) {
        this(wrapper.ownerType, varName, wrapper.modifiers, wrapper.upperBound, wrapper.parent,
                wrapper.indicator, wrapper.name, wrapper.generics,
                wrapper.inputTypes, wrapper.returnType);
    }

    public KTypeWrapper(KTypeWrapper upperBound, KTypeIndicator indicator, String name) {
        this(null, null, null, upperBound, new ArrayList<>(), indicator, name, new ArrayList<>(), new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public KTypeWrapper(KTypeIndicator indicator, String name) {
        this(null, null, null, null, new ArrayList<>(), indicator, name, new ArrayList<>(), new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public KTypeWrapper(KTypeIndicator indicator, String name, List<KTypeWrapper> generics) {
        this(null, null, null, null, new ArrayList<>(), indicator, name, generics, new ArrayList<>(), KTypeWrapper.getVoidWrapper());
    }

    public KTypeWrapper addModifiers(KTypeModifiers modifiers) {
        return new KTypeWrapper(ownerType, varName, modifiers, upperBound, parent,
                indicator, name, generics, inputTypes, returnType);
    }

    public KTypeWrapper addOwner(KTypeWrapper owner) {
        return new KTypeWrapper(owner, varName, modifiers, upperBound, parent,
                indicator, name, generics, inputTypes, returnType);
    }

    public KTypeWrapper addReturnType(KTypeWrapper returnType) {
        return new KTypeWrapper(ownerType, varName, modifiers, upperBound, parent,
                indicator, name, generics, inputTypes, returnType);
    }

    public static KTypeWrapper getVoidWrapper() {
        return new KTypeWrapper(null, null, null, null, new ArrayList<>(), KTypeIndicator.CLASS, "void", new ArrayList<>(), new ArrayList<>(), null);
    }

    public KClassType toClass(boolean open, boolean abs) {
        return new KClassType(name, open, abs);
    }

    public KInterfaceType toInterface() {
        return new KInterfaceType(name);
    }

    public KFuncType toFunction(List<KType> inputTypes, KType returnType) {
        return new KFuncType(name, generics.stream().map(wrapper -> (KGenericType) wrapper.toType()).toList(), inputTypes, returnType);
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

    private String genericUpperBoundName() {
        return upperBound == null ? KGrammarVocabulary.Any : upperBound().name();
    }

    public boolean containsName(String n) {
        return this.name.contains(n)
                || (upperBound() != null && upperBound().containsName(n))
                || generics.stream().anyMatch(g -> g.containsName(n))
                || parent.stream().anyMatch(p -> p.containsName(n))
                || (returnType != null && returnType().containsName(n));
    }

    public boolean canConvert() {
        return indicator != KTypeIndicator.UNKNOWN && inputTypes.stream().allMatch(KTypeWrapper::canConvert)
                && (upperBound == null || upperBound.canConvert())
                && generics.stream().allMatch(KTypeWrapper::canConvert)
                && parent.stream().allMatch(KTypeWrapper::canConvert)
                && (returnType == null || returnType().canConvert());
    }

    private void inferGenerics(List<KType> uncheckedGenericTypes, List<KGenericType> generics, List<KType> genericInstances) {
        for (KType uncheckedType : uncheckedGenericTypes) {
            if (uncheckedType instanceof KClassifierType) {
                genericInstances.add(uncheckedType);
                generics.add(new KGenericType(uncheckedType.name(), KTypeIndicator.CONCRETE_GENERIC, null));
            } else {
                generics.add((KGenericType) uncheckedType);
            }
        }
    }

    public KType toType() {
        switch (indicator) {
            case CLASS -> {
                List<KType> uncheckedGenericTypes = generics.stream().map(KTypeWrapper::toType).toList();
                List<KGenericType> generics = new LinkedList<>();
                List<KType> genericInstances = new LinkedList<>();
                inferGenerics(uncheckedGenericTypes, generics, genericInstances);

                return new KClassType(name, generics, genericInstances, isOpen(), isAbstract());
            }
            case INTERFACE -> {
                List<KType> uncheckedGenericTypes = generics.stream().map(KTypeWrapper::toType).toList();
                List<KGenericType> generics = new LinkedList<>();
                List<KType> genericInstances = new LinkedList<>();

                inferGenerics(uncheckedGenericTypes, generics, genericInstances);
                return new KInterfaceType(name, generics, genericInstances);
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
                KGenericType upperType = new KGenericType(genericUpperBoundName(), KTypeIndicator.CONCRETE_GENERIC, null);
                return new KGenericType(name, KTypeIndicator.CONCRETE_GENERIC, upperType);
            }
            case SYMBOLIC_GENERIC -> {
                KGenericType upperType = new KGenericType(genericUpperBoundName(), KTypeIndicator.CONCRETE_GENERIC, null);
                return new KGenericType(name, KTypeIndicator.SYMBOLIC_GENERIC, upperType);
            }
            default -> {
                throw new IllegalArgumentException("Cannot handle indicator of type: " + indicator);
            }
        }
    }

    public KCallable toCallable(KType callableOwner) {
        switch (indicator) {
            case FUNCTION -> {
                List<KType> input = inputTypes.stream().map(KTypeWrapper::toType).toList();
                KType output = returnType.toType();

                // No owner means this is a function, not a method
                if (callableOwner == null || new KVoid().equals(callableOwner)) {
                    return new KFunction(name, input, output);
                }

                if (!(callableOwner instanceof KClassifierType)) {
                    throw new IllegalArgumentException("Cannot create method callable with owner of type: " + callableOwner);
                }

                return new KMethod((KClassifierType) callableOwner, name, input, output);
            }

            case CONSTRUCTOR -> {
                List<KType> input = inputTypes.stream().map(KTypeWrapper::toType).toList();
                KType output = returnType.toType();

                if (!(output instanceof KClassifierType)) {
                    throw new IllegalArgumentException("Cannot create constructor callable with owner of type: " + output);
                }

                return new KConstructor((KClassifierType) output, input);
            }

            case CLASS, INTERFACE -> {
                KType varType = toType();

                if (!(varType instanceof KClassifierType)) {
                    throw new IllegalArgumentException("Indicator mismatch.");
                }

                if (modifiers.isConst() && modifiers.isVisibile()) {
                    return new KIdentifierCallable(callableOwner.name() + "." + varName, varType);
                }

                return null;
            }
            case CONCRETE_GENERIC, SYMBOLIC_GENERIC -> {
                KGenericType symbolicFromOwner = callableOwner.getGenerics().stream().filter(g -> g.name().equals(this.name)).toList().get(0);
                return new KIdentifierCallable(varName, symbolicFromOwner);
            }
            default -> {
                throw new IllegalArgumentException("Cannot handle indicator of type: " + indicator + " in callable creation");
            }
        }
    }
}
