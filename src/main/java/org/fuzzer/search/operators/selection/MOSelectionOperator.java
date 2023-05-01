package org.fuzzer.search.operators.selection;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.utils.Tuple;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class MOSelectionOperator extends SelectionOperator {
    abstract Map<CodeBlock, List<CodeBlock>> getDominations(List<CodeBlock> population);
}
