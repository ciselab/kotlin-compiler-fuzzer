package org.fuzzer.representations.callables;

import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.types.KType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class KCallable implements Cloneable {
    private final String name;
    private final List<KType> inputTypes;
    private final KType returnType;

    private Optional<KCallable> owner;
    private List<KCallable> lastInput = new ArrayList<>();

    public KCallable(String name, KType output) {
        this.name = name;
        this.inputTypes = new ArrayList<>();
        this.returnType = output;
        this.owner = Optional.empty();
    }

    public KCallable(String name, List<KType> input, KType output) {
        this.name = name;
        this.inputTypes = input;
        this.returnType = output;
        this.owner = Optional.empty();
    }

    public KCallable(String name, List<KType> input, KType output, KCallable owner) {
        this.name = name;
        this.inputTypes = input;
        this.returnType = output;
        this.owner = Optional.of(owner);
    }

    public String getName() {
        return name;
    }

    public List<KType> getInputTypes() {
        return inputTypes;
    }
    public KType getReturnType() {
        return returnType;
    }

    public abstract String call(Context ctx, Optional<KCallable> owner, List<KCallable> input);

    public String  call(Context ctx, Optional<KCallable> owner) {
        return call(ctx, owner, this.lastInput);
    }

    public String call(Context ctx) {
        return call(ctx, owner);
    }
    protected void updateLastInput(List<KCallable> lastInput) {
        this.lastInput = lastInput;
    }

    protected void updateOwner(Optional<KCallable> owner) {
        this.owner = owner;
    }
    public void verifyInput(Context ctx, List<KCallable> input) {
        if (!inputMatchesTypes(ctx, input))
            throw new IllegalArgumentException("Input object list for callable <" + getName() + ">: <" + input + "> does not match input types <" + this.inputTypes.toString() + ">");
    }
    private Boolean inputMatchesTypes(Context ctx, List<KCallable> input) {
        if (input.size() != inputTypes.size())
            return false;

        for (int index = 0; index < input.size(); index++) {
            if (!ctx.isSubtypeOf(input.get(index).getReturnType(), inputTypes.get(index)))
                return false;
        }

        return true;
    }

    public void verifyOwner(Context ctx, Optional<KCallable> owner) {
        if(this.owner.isPresent() != owner.isPresent()) {
            String thisOwnerPresent = (this.owner.isPresent() ? "" : "not") + " present ";
            String ownerPresent = (owner.isPresent() ? "" : "not") + " present ";
            throw new IllegalArgumentException("Owner mismatch: callable owner is " + thisOwnerPresent + " and argument owner is " + ownerPresent);
        }

        if (owner.isEmpty()) {
            return;
        }

        if (!ctx.isSubtypeOf(owner.get().getReturnType(), this.owner.get().getReturnType()))
            throw new IllegalArgumentException("Owner " + owner.get() + " is not a subtype of " + this.owner.get());
    }

    public boolean isTerminal() {
        return this.inputTypes.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KCallable kCallable)) return false;

        if (!name.equals(kCallable.name)) return false;
        if (!inputTypes.equals(kCallable.inputTypes)) return false;
        if (!returnType.equals(kCallable.returnType)) return false;
        if (!owner.equals(kCallable.owner)) return false;
        return lastInput.equals(kCallable.lastInput);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + inputTypes.hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + owner.hashCode();
        result = 31 * result + lastInput.hashCode();
        return result;
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

    public abstract boolean requiresOwner();
}
