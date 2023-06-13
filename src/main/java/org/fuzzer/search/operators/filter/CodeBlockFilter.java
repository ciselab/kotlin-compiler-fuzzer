package org.fuzzer.search.operators.filter;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.Collection;
import java.util.List;

public interface CodeBlockFilter {

    boolean accepts(CodeBlock block);

    List<CodeBlock> filter(Collection<CodeBlock> blocks);
}
