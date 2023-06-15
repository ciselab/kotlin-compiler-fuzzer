package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.representations.context.Context;

import java.util.*;

public class CodeBlock implements CodeConstruct {
    private final FuzzerStatistics stats;

    private final List<CodeSnippet> snippets;

    //func,simple_expr,do_while,assignment,try_catch,if_expr,elvis_op,simple_stmt
    //3, 6, 7, 8, 9, 10, 11, 12, refer to SampleStructure
    private static final int[] oomLanguageFeatures =  new int[]{3, 5, 7, 8, 9, 10, 11, 12};

    public CodeBlock(List<CodeSnippet> snippets) {
        this.stats = FuzzerStatistics.aggregate(snippets.stream().map(CodeSnippet::stats).toList());
        this.snippets = snippets;
        this.stats.stop();
    }

    public CodeBlock(FuzzerStatistics stats, List<CodeSnippet> snippets) {
        this.stats = stats;
        this.snippets = snippets;
    }

    public List<CodeSnippet> getSnippets() {
        return snippets;
    }

    public double[] getOomLanguageFeatures() {
        double[] allVisitations = stats().getVisitations();


        double[] selectedFeatures = new double[oomLanguageFeatures.length];
        for (int i = 0; i < oomLanguageFeatures.length; i++) {
            selectedFeatures[i] = allVisitations[oomLanguageFeatures[i]];
        }

        return selectedFeatures;
    }

    public Long size() {
        return snippets.stream().map(CodeSnippet::size).reduce(0L, Long::sum);
    }

    @Override
    public FuzzerStatistics stats() {
        return stats;
    }

    @Override
    public Set<KCallable> callableDependencies() {
        Set<KCallable> dependencies = new HashSet<>();
        for (CodeSnippet snippet : snippets) {
            dependencies.addAll(snippet.callableDependencies());
        }

        return dependencies;
    }

    @Override
    public Set<KCallable> providedCallables() {
        Set<KCallable> callables = new HashSet<>();
        for (CodeSnippet snippet : snippets) {
            callables.addAll(snippet.providedCallables());
        }

        return callables;
    }

    @Override
    public String text() {
        StringBuilder text = new StringBuilder();

        for (CodeSnippet snippet : snippets) {
            text.append(snippet.code().text());
            text.append(System.lineSeparator());
        }

        return text.toString();
    }

    public void buildContext(Context context) {
        for (CodeSnippet snippet : snippets) {
            snippet.buildContext(context);
        }
    }

    public Long getNumberOfSamples(SampleStructure s) {
        return stats.getNumberOfSamples(s);
    }

    private List<String> getCallableNames() {
        return snippets.stream().map(CodeSnippet::getCallableName).toList();
    }

    public boolean isCompatible(CodeBlock other) {
        List<String> theseCallableNames = getCallableNames();
        List<String> otherCallableNames = other.getCallableNames();

        // Code blocks are only compatible if they have no overlapping names
        return otherCallableNames.stream().noneMatch(theseCallableNames::contains);
    }

    public List<CodeSnippet> getUpStreamDependents(CodeSnippet snippet) {
        return getUpStreamDependents(new LinkedList<>(List.of(snippet)));
    }

    private List<CodeSnippet> getUpStreamDependents(List<CodeSnippet> dependentSnippets) {
        boolean selfContained = false;
        while (!selfContained) {
            selfContained = true;
            for (CodeSnippet s : dependentSnippets) {
                if (!dependentsPresent(s, dependentSnippets)) {
                    selfContained = false;
                    List<CodeSnippet> newDependencies = getDirectDependents(s);
                    dependentSnippets.addAll(newDependencies);
                    break;
                }
            }
        }

        return dependentSnippets;
    }

    public List<CodeSnippet> getDownStreamDependencies(CodeSnippet snippet) {
        return getDownStreamDependencies(List.of(snippet));
    }

    private List<CodeSnippet> getDownStreamDependencies(List<CodeSnippet> dependencySnippets) {
        boolean selfContained = false;
        while (!selfContained) {
            selfContained = true;
            for (CodeSnippet s : dependencySnippets) {
                if (!dependenciesPresent(s, dependencySnippets)) {
                    selfContained = false;
                    List<CodeSnippet> finalDependencySnippets = dependencySnippets;
                    List<CodeSnippet> newDependencies = new LinkedList<>(getDirectDependencies(s)
                            .stream()
                            .filter(sn -> !finalDependencySnippets.contains(sn))
                            .toList());
                    newDependencies.addAll(dependencySnippets);
                    dependencySnippets = newDependencies;
                    break;
                }
            }
        }

        return dependencySnippets;
    }

    /**
     * Returns a collection of (1) all snippets that
     * recursively dependent on a given `snippet` (including itself),
     * and (2) all snippets that any snippet in (1) depends on, also recursively.
     *
     * This is equivalent to an upstream traversal, followed by a downstream traversal
     * on each of the results of the upstream traversal.
     *
     * Example:
     * fun x() {}
     * fun y() { x() }
     * fun z() { y() }
     * fun a() {}
     * fun b() { y(); a() }
     *
     * getDependencyTopology(y) = {y, z, b, x, a}
     * getDependencyTopology(b) = {b, y, x}
     * getDependencyTopology(a) = { a, b, y, x }
     * @param snippet the snippet to start the upstream traversal from.
     * @return the topologically sorted list of snippets that forms from the two traversals.
     */
    public List<CodeSnippet> getDependencyTopology(CodeSnippet snippet) {
        return getDownStreamDependencies(getUpStreamDependents(snippet));
    }

    private boolean dependsOn(CodeSnippet snippet, CodeSnippet other) {
        // Whether `snippet` depends on `other`
        return other.providedCallables().stream().anyMatch(snippet.callableDependencies()::contains);
    }

    private boolean dependenciesPresent(CodeSnippet snippet, Collection<CodeSnippet> snippets) {
        Set<KCallable> providedCallables = snippets
                .stream()
                .map(CodeSnippet::providedCallables)
                .reduce(new HashSet<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });

        return providedCallables.containsAll(snippet.callableDependencies());
    }

    private boolean dependentsPresent(CodeSnippet snippet, Collection<CodeSnippet> snippets) {
        return snippets.containsAll(getDirectDependents(snippet));
    }

    private List<CodeSnippet> getDirectDependencies(CodeSnippet snippet) {
        return snippets
                .stream()
                .filter(s -> dependsOn(snippet, s))
                .toList();
    }

    private List<CodeSnippet> getDirectDependents(CodeSnippet snippet) {
        return snippets
                .stream()
                .filter(s -> dependsOn(s, snippet))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeBlock block)) return false;

        if (!Objects.equals(stats, block.stats)) return false;
        return Objects.equals(snippets, block.snippets);
    }

    @Override
    public int hashCode() {
        int result = stats != null ? stats.hashCode() : 0;
        result = 31 * result + (snippets != null ? snippets.hashCode() : 0);
        return result;
    }
}
