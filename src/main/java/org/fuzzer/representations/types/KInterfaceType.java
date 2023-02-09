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
    public boolean canBeInherited() {
        return false;
    }

    @Override
    public boolean canBeInstantiated() {
        return false;
    }

    @Override
    public boolean canBeDeclared() {
        return true;
    }
}
