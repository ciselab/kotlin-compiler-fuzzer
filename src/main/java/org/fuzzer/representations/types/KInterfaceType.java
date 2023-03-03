package org.fuzzer.representations.types;

import java.util.List;

public class KInterfaceType extends KClassifierType {

    public KInterfaceType(String name, List<KGenericType> generics) {
        super(name, generics);
    }

    public KInterfaceType(String name) {
        super(name);
    }

    @Override
    public KInterfaceType withNewName(String name) {
        return new KInterfaceType(name, getGenerics(), genericInstances);
    }

    @Override
    public KInterfaceType withNewGenericInstances(List<KType> genericInstances) {
        return new KInterfaceType(name(), getGenerics(), genericInstances);
    }

    public KInterfaceType(String name, List<KGenericType> generics, List<KType> genericInstances) {
        super(name, generics, genericInstances);
    }

    @Override
    public boolean canBeInherited() {
        return false;
    }

    @Override
    public boolean canBeInstantiated() {
        return false;
    }

    @Override
    public boolean canBeDeclared() {
        return getGenerics().stream().noneMatch(KGenericType::isSymbolic);
    }

    @Override
    public String toString() {
        return "[interface]: " + name() + (getGenerics().isEmpty() ? "" : "<" + getGenerics() + ">");
    }
}
