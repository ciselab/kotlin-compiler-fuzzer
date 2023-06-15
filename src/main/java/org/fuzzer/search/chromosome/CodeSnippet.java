package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.context.Context;

import java.util.Set;

public record CodeSnippet(CodeFragment code,
                          String name,
                          Set<KCallable> callableDependencies,

                          KCallable providedCallable,
                          FuzzerStatistics stats,

                          SnippetType snippetType) implements CodeConstruct {

    public CodeSnippet() {
        this(new CodeFragment(), "", Set.of(), null, null, SnippetType.UNKNOWN);
    }

    public CodeSnippet withNewType(SnippetType snippetType) {
        return new CodeSnippet(this.code, this.name, this.callableDependencies, this.providedCallable, this.stats, snippetType);
    }

    public static CodeSnippet emptySnippetOfType(SnippetType fragmentType) {
        return new CodeSnippet(new CodeFragment(), "", Set.of(), null, null, fragmentType);
    }

    public long size() {
        return code.size();
    }

    public void buildContext(Context context) {
        context.addIdentifier(name, providedCallable);
    }

    public String getCallableName() {
        return providedCallable.getName();
    }

    @Override
    public Set<KCallable> providedCallables() {
        return Set.of(providedCallable);
    }

    @Override
    public String text() {
        return code().text();
    }

    public CodeSnippet getCopy() {
        return new CodeSnippet(code, name, callableDependencies, providedCallable, stats, snippetType);
    }
}
