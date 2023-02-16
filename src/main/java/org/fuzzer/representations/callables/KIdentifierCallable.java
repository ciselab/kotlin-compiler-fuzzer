package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.Optional;

public class KIdentifierCallable extends KCallable {


    public KIdentifierCallable(String id, KType returnType) {
        super(id, returnType);
    }

    @Override
    public String call(Context ctx, KCallable owner, List<KCallable> input) {
        return getName();
    }

    @Override
    public boolean requiresOwner() {
        return false;
    }
}
