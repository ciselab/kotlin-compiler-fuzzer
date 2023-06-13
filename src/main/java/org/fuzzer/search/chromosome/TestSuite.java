package org.fuzzer.search.chromosome;

import org.fuzzer.utils.RandomNumberGenerator;

import java.util.List;
import java.util.Objects;

public class TestSuite {
    private final List<CodeBlock> blocks;

    public TestSuite(List<CodeBlock> blocks) {
        this.blocks = blocks;
    }

    public List<CodeBlock> getBlocks() {
        return blocks;
    }

    public int size() {
        return blocks.size();
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public void add(CodeBlock b) {
        this.blocks.add(b);
    }

    public void remove(RandomNumberGenerator rng) {
        if (this.isEmpty()) {
            return;
        }

        blocks.remove(rng.selectFromList(blocks));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestSuite testSuite)) return false;

        return Objects.equals(blocks, testSuite.blocks);
    }

    @Override
    public int hashCode() {
        return blocks != null ? blocks.hashCode() : 0;
    }
}
