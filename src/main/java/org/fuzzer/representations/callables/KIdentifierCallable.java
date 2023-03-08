package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.Optional;

public class KIdentifierCallable extends KCallable {

    private final boolean mutable;

    public KIdentifierCallable(String id, KType returnType) {
        super(id, returnType);
        this.mutable = true;
    }

    public KIdentifierCallable(String id, KType returnType, boolean mutable) {
        super(id, returnType);
        this.mutable = mutable;
    }

    @Override
    public String call(Context ctx, KCallable owner, List<KCallable> input) {
        return getName();
    }

    @Override
    public boolean requiresOwner() {
        return false;
    }

    public boolean isMutable() {
        return mutable;
    }
}
