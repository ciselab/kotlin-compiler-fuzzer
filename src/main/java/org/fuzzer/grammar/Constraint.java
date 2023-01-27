package org.fuzzer.grammar;

public abstract class Constraint {

    public Constraint() {

    }

    abstract public boolean holds(Context context);
}
