package org.fuzzer.representations.types;

import java.util.List;
import java.util.Optional;

public record KTypeWrapper(String name,
                           List<KType> inputTypes,
                           List<KGenericType> generics,
                           Optional<KType> returnType) {

}
