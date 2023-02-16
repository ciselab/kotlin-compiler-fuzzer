package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.representations.types.KType;

import java.util.List;
public class KConstructor extends KMethod {

    public KConstructor(KClassType kClass, List<KType> inputTypes) {
        super(kClass, "", inputTypes, kClass);
    }

    @Override
    public void verifyOwner(Context ctx, KCallable owner) {
        if (owner != null) {
            throw new IllegalArgumentException("The owner of a constructor is implicit.");
        }
    }
}
