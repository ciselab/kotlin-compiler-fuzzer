package org.fuzzer.search.operators.filter;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.Collection;
import java.util.List;

public class SizeFilter implements CodeBlockFilter {
    private final Long upperThreshold;

    public SizeFilter(Long upperThreshold) {
        this.upperThreshold = upperThreshold;
    }


    @Override
    public boolean accepts(CodeBlock block) {
        return block.size() <= upperThreshold;
    }

    @Override
    public List<CodeBlock> filter(Collection<CodeBlock> blocks) {
        return blocks.stream().filter(this::accepts).toList();
    }
}
