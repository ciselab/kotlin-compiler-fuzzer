package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KType;

import java.util.List;

public interface IdentifierStore {

    boolean isEmpty();
    List<KCallable> identifiersOfType(KType type);

    boolean hasIdentifier(String identifier);

    KCallable getIdentifier(String identifier);

    String randomIdentifier();

    void addIdentifier(String identifier, KCallable callable);

    void updateIdentifier(String identifier, KCallable callable);

    KType typeOfIdentifier(String id);

    public List<KCallable> callablesOfType(KType returnType);
}
