package org.fuzzer.representations.callables;

import org.fuzzer.grammar.Context;
import org.fuzzer.representations.types.KType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class KCallable implements Cloneable {
    private final String name;
    private final List<KType> inputTypes;
    private final KType outputType;

    private List<KCallable> lastInput = new ArrayList<>();
    public KCallable() {
        this.name = null;
        this.inputTypes = new ArrayList<>();
        this.outputType = new KType("Any");
    }

    public KCallable(String name, KType output) {
        this.name = name;
        this.inputTypes = new ArrayList<>();
        this.outputType = output;
    }

    public KCallable(String name, List<KType> input, KType output) {
        this.name = name;
        this.inputTypes = input;
        this.outputType = output;
    }

    public KCallable(KCallable other) {
        this.name = other.name;
        this.inputTypes = other.inputTypes;
        this.outputType = other.outputType;
        this.lastInput = List.copyOf(other.lastInput);
    }

    public String getName() {
        return name;
    }

    public List<KType> getInputTypes() {
        return inputTypes;
    }
    public KType getOutputType() {
        return outputType;
    }

    public abstract String call(Context ctx, List<KCallable> input);

    public String call(Context ctx) {
        return call(ctx, this.lastInput);
    }

    protected void updateLastInput(List<KCallable> lastInput) {
        this.lastInput = lastInput;
    }

    public void verifyInput(Context ctx, List<KCallable> input) {
        if (!inputMatchesTypes(ctx, input))
            throw new IllegalArgumentException("Input object list for callable <" + getName() + ">: <" + input + "> does not match input types <" + this.inputTypes.toString() + ">");

    }
    private Boolean inputMatchesTypes(Context ctx, List<KCallable> input) {
        if (input.size() != inputTypes.size())
            return false;

        for (int index = 0; index < input.size(); index++) {
            if (!ctx.isSubtypeOf(input.get(index).getOutputType(), inputTypes.get(index)))
                return false;
        }

        return true;
    }

    public boolean isTerminal() {
        return this.inputTypes.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KCallable kCallable)) return false;

        if (!Objects.equals(name, kCallable.name)) return false;
        if (!Objects.equals(inputTypes, kCallable.inputTypes)) return false;
        return Objects.equals(outputType, kCallable.outputType);
    }

    @Override
    public String toString() {
        return "<" + getName() + ">";
    }

    public Object clone() throws CloneNotSupportedException {
        Object clone = super.clone();
        KCallable newCallable = (KCallable) clone;
        newCallable.lastInput = List.copyOf(lastInput);

        return newCallable;
    }
}
