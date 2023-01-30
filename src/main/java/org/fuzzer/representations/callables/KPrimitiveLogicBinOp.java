package org.fuzzer.representations.callables;

import org.fuzzer.grammar.Context;
import org.fuzzer.representations.types.KType;

import java.util.Arrays;
import java.util.List;

public class KPrimitiveLogicBinOp extends KCallable {
    public KPrimitiveLogicBinOp(String name) {

        super(name, Arrays.asList(new KType("Boolean"), new KType("Boolean")), new KType("Boolean"));
    }

    @Override
    public String call(Context ctx, List<KCallable> input) {
        verifyInput(ctx, input);
        updateLastInput(input);
        String pre = input.get(0).call(ctx);
        String post = input.get(1).call(ctx);

        return pre + " " + getName() + " " + post;
    }
}
