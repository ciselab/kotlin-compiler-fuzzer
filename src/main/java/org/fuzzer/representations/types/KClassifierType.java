package org.fuzzer.representations.types;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public abstract class KClassifierType implements KType {
    private final String name;

    private final List<KGenericType> generics;

    protected final List<KType> genericInstances;

    public KClassifierType(String name, List<KGenericType> generics, List<KType> genericInstances) {
        this.name = name;
        this.generics = generics;
        this.genericInstances = genericInstances;
    }

    protected KClassifierType(String name, List<KGenericType> generics) {
        this.name = name;
        this.generics = generics;
        this.genericInstances = new ArrayList<>();
    }

    public KClassifierType(String name) {
        this.name = name;
        this.generics = new ArrayList<>();
        this.genericInstances = new ArrayList<>();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<KType> getInputTypes() {
        return new ArrayList<>();
    }

    public KType getReturnType() {
        return new KVoid();
    }

    @Override
    public List<KGenericType> getGenerics() {
        return generics;
    }

    public abstract KClassifierType withNewName(String name);

    public abstract KClassifierType withNewGenericInstances(List<KType> genericInstances);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KClassifierType that)) return false;

        if (!name.equals(that.name)) return false;
        return generics.equals(that.generics);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + generics.hashCode();
        return result;
    }

    public String toString() {
        if (generics.isEmpty()) {
            return name;
        }

        StringBuilder genericRepr = new StringBuilder();
        genericRepr.append("<");

        ListIterator<KGenericType> genericIterator = generics.listIterator();

        while(genericIterator.hasNext()) {
            genericRepr.append(genericIterator.next());

            if (genericIterator.hasNext()) {
                genericRepr.append(",");
            }
        }

        genericRepr.append(">");

        return name + genericRepr;
    }

    public String codeRepresentation(List<KType> overridenGenericInstances) {
        StringBuilder genericSection = new StringBuilder();
        if (!generics.isEmpty()) {
            genericSection.append("<");

            List<? extends KType> typesToIterate = overridenGenericInstances != null ? overridenGenericInstances : genericInstances.isEmpty() ? generics : genericInstances;
            for (int i = 0; i < typesToIterate.size() - 1; i++) {
                genericSection.append(typesToIterate.get(i).codeRepresentation()).append(",");
            }

            genericSection.append(typesToIterate.get(typesToIterate.size() - 1).codeRepresentation());
            genericSection.append(">");
        }

        return name + genericSection;
    }

    @Override
    public String codeRepresentation() {
        return codeRepresentation(null);
    }
}
