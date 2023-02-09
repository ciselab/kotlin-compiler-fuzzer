package org.fuzzer.representations.callables;

import kotlin.reflect.KClass;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class KConstructor extends KMethod {

    public KConstructor(KClassType kClass, List<KType> inputTypes) {
        super(kClass, "", inputTypes, kClass);
    }

    @Override
    public void verifyOwner(Context ctx, Optional<KCallable> owner) {
        if (owner.isPresent()) {
            throw new IllegalArgumentException("The owner of a constructor is implicit.");
        }
    }
}
