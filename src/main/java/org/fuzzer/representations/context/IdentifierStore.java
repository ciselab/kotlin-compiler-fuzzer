package org.fuzzer.representations.context;

import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.types.KType;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface IdentifierStore {
    List<KCallable> callablesOfType();

    boolean isEmpty();

    boolean containsIdentifier(String identifier);

    Optional<String> randomIdentifier();

    void addIdentifier(String identifier, KCallable callable);

    Optional<KCallable>  typeOfIdentifier();

    Optional<KCallable> callableOfType(KType returnType);

    Optional<KCallable> terminalCallableOfType(KType returnType);

    Optional<KCallable> consumerCallableOfType(KType returnType);
}
