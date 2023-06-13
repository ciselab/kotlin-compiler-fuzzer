package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.callables.KCallable;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<CodeBlock> split() {
        List<CodeBlock> blocksWithin = new LinkedList<>();

        for (CodeSnippet snippet : snippets) {
            blocksWithin.add(new CodeBlock(getDependencySnippets(snippet, snippets).stream().toList()));
        }

        return blocksWithin;
    }

    private Set<CodeSnippet> getDependencySnippets(CodeSnippet snippet,
                                                   Collection<CodeSnippet> visibleSnippets) {
        Set<CodeSnippet> dependencySnippets = new HashSet<>();
        dependencySnippets.add(snippet);

        return getDependencySnippets(visibleSnippets, dependencySnippets);
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

    private Set<CodeSnippet> getDependencySnippets(Collection<CodeSnippet> visibleSnippets,
                                                   Set<CodeSnippet> dependencySet) {
        boolean selfContained = false;
        while (!selfContained) {
            selfContained = true;
            for (CodeSnippet snippet : visibleSnippets) {
                if (!dependenciesPresent(snippet, dependencySet)) {
                    selfContained = false;
                    dependencySet.addAll(getDirectDependencies(snippet, visibleSnippets));
                    break;
                }
            }
        }

        return dependencySet;
    }

    private Set<CodeSnippet> getDirectDependencies(CodeSnippet snippet,
                                                   Collection<CodeSnippet> visibleSnippets) {
        return visibleSnippets
                .stream()
                .filter(s -> providedCallables().stream().anyMatch(s.callableDependencies()::contains))
                .collect(Collectors.toSet());
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
