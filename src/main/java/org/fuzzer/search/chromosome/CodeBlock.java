package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.callables.KCallable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CodeBlock {
    private final FuzzerStatistics stats;

    private final CodeFragment text;

    private final Set<KCallable> callables;

    public CodeBlock(String name, List<CodeSnippet> snippets, Set<KCallable> dependencies) {
        this.stats = FuzzerStatistics.aggregate(snippets.stream().map(CodeSnippet::stats).toList());
        this.callables = dependencies;
        this.text = snippets.stream().map(CodeSnippet::code).reduce(new CodeFragment(),  (x, y) -> { x.extend(y); return x; });
        text.setName(name);
        this.stats.stop();
    }

    public CodeBlock(FuzzerStatistics stats, CodeFragment text, Set<KCallable> callables) {
        this.stats = stats;
        this.text = text;
        this.callables = callables;
    }

    public String getName() {
        return text.getName();
    }

    public Long size() {
        return (long) text.getText().length();
    }

    public FuzzerStatistics getStats() {
        return stats;
    }

    public CodeFragment getText() {
        return text;
    }

    public Set<KCallable> getCallables() {
        return callables;
    }

    public Long getNumberOfSamples(SampleStructure s) {
        return stats.getNumberOfSamples(s);
    }

    public boolean isCompatible(CodeBlock other) {
        List<String> callableNames = callables.stream().map(KCallable::getName).toList();

        // Code blocks are only compatible if they have no overlapping names
        // TODO generalize this by having each block retain the snippets that compose it
        return other.getCallables().stream().noneMatch(kCallable -> callableNames.contains(kCallable.getName()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeBlock codeBlock)) return false;

        if (!Objects.equals(stats, codeBlock.stats)) return false;
        if (!Objects.equals(text, codeBlock.text)) return false;
        return Objects.equals(callables, codeBlock.callables);
    }

    @Override
    public int hashCode() {
        int result = stats != null ? stats.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (callables != null ? callables.hashCode() : 0);
        return result;
    }
}
