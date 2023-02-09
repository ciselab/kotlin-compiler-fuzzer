package org.fuzzer.grammar;

import org.fuzzer.representations.context.Context;

public abstract class Constraint {

    public Constraint() {

    }

    abstract public boolean holds(Context context);
}
