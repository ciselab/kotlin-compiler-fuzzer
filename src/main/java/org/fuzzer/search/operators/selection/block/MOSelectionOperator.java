package org.fuzzer.search.operators.selection.block;

import org.fuzzer.search.chromosome.CodeBlock;

import java.util.List;
import java.util.Map;

public abstract class MOSelectionOperator extends SelectionOperator {
    abstract Map<CodeBlock, List<CodeBlock>> getDominations(List<CodeBlock> population);
}
