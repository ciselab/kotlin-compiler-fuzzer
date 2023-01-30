package org.fuzzer.grammar;

public class IdentifierExistsConstraint extends Constraint {
    private final String identifier;

    public IdentifierExistsConstraint(String identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public boolean holds(Context context) {
        return context.containsIdentifier(this.identifier);
    }
}
