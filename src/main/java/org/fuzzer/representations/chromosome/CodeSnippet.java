package org.fuzzer.representations.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;

import java.util.Set;

public record CodeSnippet(CodeFragment code,
                          String name,
                          Set<String> callableDependencies,
                          FuzzerStatistics stats) {}
