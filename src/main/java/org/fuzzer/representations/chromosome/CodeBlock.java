package org.fuzzer.representations.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.callables.KCallable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeBlock {
    private final FuzzerStatistics stats;

    private final CodeFragment text;

    private final Set<KCallable> callables;

    public CodeBlock(List<CodeSnippet> snippets, Set<KCallable> dependencies) {
        this.stats = FuzzerStatistics.aggregate(snippets.stream().map(CodeSnippet::stats).toList());
        this.callables = dependencies;
        this.text = snippets.stream().map(CodeSnippet::code).reduce(new CodeFragment(),  (x, y) -> { x.extend(y); return x; });
        this.stats.stop();
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
}
