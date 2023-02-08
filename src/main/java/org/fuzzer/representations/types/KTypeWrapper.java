package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record KTypeWrapper(KTypeIndicator indicator,
                           String name,
                           List<KGenericType> generics,
                           List<KTypeWrapper> inputTypes,
                           Optional<KTypeWrapper> returnType) {

    public KTypeWrapper(KTypeIndicator indicator, String name) {
        this(indicator, name, new ArrayList<>(), new ArrayList<>(), Optional.empty());
    }

    public KClassType toClass(boolean open, boolean abs) {
        return new KClassType(name, open, abs);
    }

    public KInterfaceType toInterface() {
        return new KInterfaceType(name);
    }

    public KFuncType toFunction(List<KType> inputTypes, Optional<KType> returnType) {
        return returnType.isPresent() ?
                new KFuncType(name, generics, inputTypes, returnType.get()) :
                new KFuncType(name, generics, inputTypes);
    }

    public KType toType(Optional<Boolean> open, Optional<Boolean> abs) {
        switch (indicator) {
            case CLASS -> {

                // TODO: handle this in parsing. For now, assume dummy values.
                if (!(open.isPresent() && abs.isPresent())) {
//                    throw new IllegalArgumentException("Cannot instantiate class without modifiers.");
                    return toClass(true, false);
                }
                return toClass(open.get(), abs.get());
            }
            case INTERFACE -> {
                return toInterface();
            }
            case FUNCTION -> {
                List<KType> inputTypes = inputTypes().stream()
                        .map(typeWrapper -> typeWrapper.toType(open, abs))
                        .toList();

                Optional<KType> returnType = this.returnType.isPresent() ?
                        Optional.of(this.returnType.get().toType(open, abs)) :
                        Optional.empty();

                return toFunction(inputTypes, returnType);
            }
            default -> {
                throw new IllegalArgumentException("Cannot handle indicator of type: " + indicator);
            }
        }
    }

    public KType toType() {
        return toType(Optional.empty(), Optional.empty());
    }
}
