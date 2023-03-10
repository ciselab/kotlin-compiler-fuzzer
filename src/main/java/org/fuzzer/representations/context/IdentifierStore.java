package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KType;

import java.io.Serializable;
import java.util.List;

public interface IdentifierStore extends Serializable {

    boolean isEmpty();

    boolean hasAssignableIdentifiers();

    List<KCallable> allIdentifiers();

    List<KCallable> allAssignableIdentifiers();

    String randomAssignableIdentifier();

    List<KCallable> identifiersOfType(KType type);

    boolean hasIdentifier(String identifier);

    KCallable getIdentifier(String identifier);

    String randomIdentifier();

    void addIdentifier(String identifier, KCallable callable);

    void updateIdentifier(String identifier, KCallable callable);

    KType typeOfIdentifier(String id);

    public List<KCallable> callablesOfType(KType returnType);
}
