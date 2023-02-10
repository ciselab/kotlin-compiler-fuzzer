package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.Optional;

public class KAnonymousCallable extends KCallable {

    public KAnonymousCallable(String representation, KType returnType) {
        super(representation, returnType);
    }

    @Override
    public String call(Context ctx, Optional<KCallable> owner, List<KCallable> input) {
        return getName();
    }

    @Override
    public boolean requiresOwner() {
        return false;
    }
}
