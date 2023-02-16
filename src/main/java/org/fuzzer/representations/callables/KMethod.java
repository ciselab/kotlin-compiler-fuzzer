package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class KMethod extends KCallable {

    KClassifierType ownerType;

    public KMethod(KClassifierType ownerType,
                   String name,
                   List<KType> input,
                   KType output) {
        super(name, input, output);
        this.ownerType = ownerType;
    }

    public KClassifierType getOwnerType() {
        return ownerType;
    }

    @Override
    public String call(Context ctx, KCallable owner, List<KCallable> input) {
        super.verifyInput(ctx, input);
        verifyOwner(ctx, owner);

        updateLastInput(input);
        updateOwner(owner);

        // Call the owner without any parameters
        String ownerRepr = owner.call(ctx);
        StringBuilder argList = new StringBuilder();

        ListIterator<KCallable> iter = input.listIterator();

        while(iter.hasNext()) {
            KCallable nextCallable = iter.next();
            argList.append(nextCallable.call(ctx));

            if (iter.hasNext()) {
                argList.append(", ");
            }
        }

        return ownerRepr + "." + getName() + "(" + argList + ")";
    }

    @Override
    public void verifyOwner(Context ctx, KCallable owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Cannot call a method without specifying its owner.");
        }

        if (!ctx.isSubtypeOf(owner.getReturnType(), this.ownerType))
            throw new IllegalArgumentException("Owner " + owner + " is not a subtype of " + this.ownerType);
    }

    @Override
    public boolean requiresOwner() {
        return true;
    }
}
