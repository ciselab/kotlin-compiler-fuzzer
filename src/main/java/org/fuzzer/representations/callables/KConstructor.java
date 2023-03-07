package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.ListIterator;

public class KConstructor extends KMethod {

    public KConstructor(KClassifierType kClass, List<KType> inputTypes) {
        super(kClass, "", inputTypes, kClass);
    }

    @Override
    public void verifyOwner(Context ctx, KCallable owner) {
        if (owner != null) {
            throw new IllegalArgumentException("The owner of a constructor is implicit.");
        }
    }

    @Override
    public String call(Context ctx, KCallable owner, List<KCallable> input) {
        verifyInput(ctx, input);
        verifyOwner(ctx, owner);

        updateLastInput(input);
        updateOwner(owner);

        // Call the owner without any parameters
        StringBuilder argList = new StringBuilder();

        ListIterator<KCallable> iter = input.listIterator();

        while(iter.hasNext()) {
            KCallable nextCallable = iter.next();
            argList.append(nextCallable.call(ctx));

            if (iter.hasNext()) {
                argList.append(", ");
            }
        }

        return getReturnType().name() + "(" + argList + ")";
    }
}
