package org.fuzzer.representations.callables;

import org.fuzzer.grammar.Context;
import org.fuzzer.representations.types.KType;

import java.util.List;

public class KIdentifierCallable extends KCallable {
    public KIdentifierCallable(String id, KType type) {
        super(id, type);
    }

    @Override
    public String call(Context ctx, List<KCallable> input) {
        verifyInput(ctx, input);
        // No need to save input, as it would alwasy be empty.
        return getName();
    }
}
