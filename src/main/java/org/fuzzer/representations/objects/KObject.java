package org.fuzzer.representations.objects;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class KObject {
    private final KType type;

    private final List<KCallable> callables;

    public KObject(KType type, List<KCallable> callables) {
        this.type = type;
        this.callables = callables;
    }

    public KObject(KType type) {
        this.type = type;
        this.callables = new ArrayList<>();
    }

    public KType getType() {
        return type;
    }

    public List<KCallable> getCallables() {
        return callables;
    }

    public abstract String textulRepresentation();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KObject kObject)) return false;

        if (!Objects.equals(type, kObject.type)) return false;
        return Objects.equals(callables, kObject.callables);
    }
}
