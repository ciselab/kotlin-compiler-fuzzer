package org.fuzzer.representations.callables;

import org.fuzzer.grammar.Context;
import org.fuzzer.representations.types.KType;

import java.util.List;

public class KAnonymousCallable extends KCallable {

    private final String representation;
    public KAnonymousCallable(KType type, String representation) {
        super("", type);
        this.representation = representation;
    }

    @Override
    public String call(Context ctx, List<KCallable> input) {
        verifyInput(ctx, input);
        // No need to save input, as it would alwasy be empty.
        return representation;
    }

    @Override
    public String toString() {
        return "[" + "anon:" + representation + "]";
    }
}
