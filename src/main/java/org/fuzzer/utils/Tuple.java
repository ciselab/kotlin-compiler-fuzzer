package org.fuzzer.utils;

import java.io.Serializable;

public record Tuple<T>(T first, T second) implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple<?> tuple)) return false;

        if (!first.equals(tuple.first)) return false;
        return second.equals(tuple.second);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
