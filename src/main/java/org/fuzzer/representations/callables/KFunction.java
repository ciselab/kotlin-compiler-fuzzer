package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

// TODO use this in KMethod
public class KFunction extends KCallable {

    public KFunction(String name, List<KType> input, KType output) {
        super(name, input, output);
    }

    @Override
    public String call(Context ctx, KCallable owner, List<KCallable> input) {
        super.verifyInput(ctx, input);
        updateLastInput(input);

        StringBuilder argList = new StringBuilder();

        ListIterator<KCallable> iter = input.listIterator();

        while(iter.hasNext()) {
            KCallable nextCallable = iter.next();
            argList.append(nextCallable.call(ctx));

            if (iter.hasNext()) {
                argList.append(", ");
            }
        }

        return getName() + "(" + argList + ")";
    }

    @Override
    public boolean requiresOwner() {
        return false;
    }
}
