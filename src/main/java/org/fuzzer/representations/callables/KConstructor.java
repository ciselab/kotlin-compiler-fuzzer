package org.fuzzer.representations.callables;

import kotlin.reflect.KClass;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.representations.types.KGenericType;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.Optional;

public class KConstructor extends KCallable {

    public KClassType kClass;

    List<KType> inputTypes;

    public KConstructor(KClassType kClass, List<KType> inputTypes) {
        super(kClass.name() + ".constructor", inputTypes, kClass);
        this.kClass = kClass;
    }

    @Override
    public String call(Context ctx, Optional<KCallable> owner, List<KCallable> input) {
        return null;
    }
}
