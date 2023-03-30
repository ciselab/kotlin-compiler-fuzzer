package org.fuzzer.search.operators.selection;

import org.fuzzer.representations.chromosome.CodeBlock;

import java.util.List;

public interface SelectionOperator {
    List<CodeBlock> select(List<CodeBlock> population, int numberOfSelections);
}
