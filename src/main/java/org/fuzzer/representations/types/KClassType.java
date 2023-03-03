package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;

public class KClassType extends KClassifierType {

    private final boolean open;

    public final boolean abs;

    public KClassType(String name, boolean open, boolean abs) {
        super(name);
        this.open = open;
        this.abs = abs;
    }

    public KClassType(String name, List<KGenericType> generics, boolean open, boolean abs) {
        super(name, generics);
        this.open = open;
        this.abs = abs;
    }

    public KClassType(String name, List<KGenericType> generics, List<KType> genericInstances, boolean open, boolean abs) {
        super(name, generics, genericInstances);
        this.open = open;
        this.abs = abs;
    }

    @Override
    public boolean canBeInherited() {
        return open;
    }

    @Override
    public boolean canBeInstantiated() {
        return !abs;
    }

    @Override
    public boolean canBeDeclared() {
        return getGenerics().stream().noneMatch(KGenericType::isSymbolic);
    }

    @Override
    public KClassType withNewName(String name) {
        return new KClassType(name, getGenerics(), genericInstances, open, abs);
    }

    @Override
    public KClassType withNewGenericInstances(List<KType> genericInstances) {
        return new KClassType(name(), getGenerics(), genericInstances, open, abs);
    }

    // TODO: handle modifiers properly
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KClassType that)) return false;
        if (!super.equals(o)) return false;

        return true;
//        if (open != that.open) return false;
//        return abs == that.abs;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
//        result = 31 * result + (open ? 1 : 0);
//        result = 31 * result + (abs ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[class]: " + name() + (getGenerics().isEmpty() ? "" : "<" + getGenerics() + ">");
    }
}
