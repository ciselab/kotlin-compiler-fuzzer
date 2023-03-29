package org.fuzzer.representations.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.representations.callables.KCallable;

import java.util.List;

public class CodeSnippet {
    private final CodeFragment code;

    private final String name;

    private final List<String> callableDependencies;

    private final FuzzerStatistics stats;

    public CodeSnippet(CodeFragment code, String name, List<String> callableDependencies, FuzzerStatistics stats) {
        this.code = code;
        this.name = name;
        this.callableDependencies = callableDependencies;
        this.stats = stats;
    }

    public CodeFragment getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<String> getCallableDependencies() {
        return callableDependencies;
    }

    public FuzzerStatistics getStats() {
        return stats;
    }
}
