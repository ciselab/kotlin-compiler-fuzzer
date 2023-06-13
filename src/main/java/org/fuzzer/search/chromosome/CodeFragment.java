package org.fuzzer.search.chromosome;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.callables.KCallable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record CodeFragment(String text, Set<KCallable> callableDependencies, FragmentType fragmentType) implements CodeConstruct {

    public CodeFragment() {
        this("", new HashSet<>(), FragmentType.UNKNOWN);
    }

    public CodeFragment withNewType(FragmentType fragmentType) {
        return new CodeFragment(this.text, this.callableDependencies, fragmentType);
    }

    public CodeFragment withNewDependencies(Set<KCallable> callableDependencies) {
        return new CodeFragment(this.text, callableDependencies, this.fragmentType);
    }

    public static CodeFragment emptyFragmentOfType(FragmentType fragmentType) {
        return new CodeFragment("", new HashSet<>(), fragmentType);
    }

    public static CodeFragment textCodeFragment(String text) {
        return new CodeFragment(text, new HashSet<>(), FragmentType.UNKNOWN);
    }

    public CodeFragment(List<CodeFragment> fragments, FragmentType fragmentType) {
        this(fragments.stream()
                        .map(CodeFragment::text).reduce("", String::concat),
                fragments.stream()
                        .map(CodeFragment::callableDependencies)
                        .reduce(new HashSet<>(), (acc, s) -> { acc.addAll(s); return acc;}),
                fragmentType);
    }
    public Long size() {
        return (long) this.text.length();
    }

    public CodeFragment append(CodeFragment code) {
        Set<KCallable> combinedDependencies = new HashSet<>();
        combinedDependencies.addAll(this.callableDependencies);
        combinedDependencies.addAll(code.callableDependencies());

        return new CodeFragment(this.text + code.text(), combinedDependencies, this.fragmentType);
    }

    public CodeFragment append(String code) {
        return new CodeFragment(this.text + code, callableDependencies(), this.fragmentType);
    }

    public CodeFragment extend(CodeFragment code) {
        Set<KCallable> combinedDependencies = new HashSet<>();
        combinedDependencies.addAll(this.callableDependencies);
        combinedDependencies.addAll(code.callableDependencies());

        return new CodeFragment(this.text + System.lineSeparator() + code.text(), combinedDependencies, this.fragmentType);
    }

    public CodeFragment extend(String code) {
        return new CodeFragment(this.text + System.lineSeparator() + code, callableDependencies(), this.fragmentType);
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeFragment that)) return false;

        if (!Objects.equals(text, that.text)) return false;
        return fragmentType == that.fragmentType;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (fragmentType != null ? fragmentType.hashCode() : 0);
        return result;
    }

    @Override
    public FuzzerStatistics stats() {
        return new FuzzerStatistics();
    }

    @Override
    public Set<KCallable> providedCallables() {
        return new HashSet<>();
    }
}
