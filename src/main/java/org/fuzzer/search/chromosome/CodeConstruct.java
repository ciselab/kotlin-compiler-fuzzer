package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.callables.KCallable;

import java.util.*;

public interface CodeConstruct {

    FuzzerStatistics stats();

    Set<KCallable> callableDependencies();

    Set<KCallable> providedCallables();

    String text();

    static CodeBlock aggregateConstructs(List<CodeConstruct> constructs) {
        List<CodeSnippet> snippets = new LinkedList<>();

        for (CodeConstruct construct : constructs) {
            if (construct instanceof CodeFragment) {
                throw new IllegalArgumentException("Fragment aggregation is not yet supported.");
            }

            if (construct instanceof CodeSnippet snippet) {
                snippets.add(snippet);
            }

            if (construct instanceof CodeBlock block) {
                snippets.addAll(block.getSnippets());
            }
        }

        if (!isCompatible(snippets)) {
            throw new IllegalArgumentException("Cannot aggregate incompatible snippets.");
        }

        return new CodeBlock(snippets);
    }

    static boolean isCompatible(Collection<CodeSnippet> snippets) {
        for (CodeSnippet snippet : snippets) {
            Set<KCallable> providedCallables = snippet.providedCallables();
            Set<KCallable> dependencies = snippet.callableDependencies();

            // Check if other snippets provide the same dependencies
            for (KCallable callable : providedCallables) {
                boolean uniqueCallable = snippets.stream()
                        .filter(x -> !snippet.equals(x))
                        .noneMatch(s -> s.callableDependencies().contains(callable));

                if (!uniqueCallable) {
                    return false;
                }
            }

            for (KCallable callable : dependencies) {
                boolean dependencyPresent = snippets.stream()
                        .anyMatch(s -> s.providedCallables().contains(callable));

                if (!dependencyPresent) {
                    return false;
                }
            }
        }

        return true;
    }
}
