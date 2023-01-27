package org.fuzzer.representations.objects;

import org.fuzzer.representations.types.KType;

public class KIntegralNumberObj extends KObject {

    private final Long value;
    public KIntegralNumberObj(KType type, Long value) {
        super(type);
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String textulRepresentation() {
        return value.toString();
    }
}
