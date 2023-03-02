package org.fuzzer.utils;

import java.io.Serializable;
import java.util.List;

public record Label<T>(List<T> conditions) implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label<?> label)) return false;

        return conditions.equals(label.conditions);
    }

    @Override
    public int hashCode() {
        return conditions.hashCode();
    }
}
