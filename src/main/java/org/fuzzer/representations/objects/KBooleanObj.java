package org.fuzzer.representations.objects;

import org.fuzzer.representations.types.KType;

public class KBooleanObj extends KObject {
    private final Boolean value;
    public KBooleanObj(KType type, Boolean value) {
        super(type);
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public String textulRepresentation() {
        return value.toString();
    }
}
