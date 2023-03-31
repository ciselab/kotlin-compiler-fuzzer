package org.fuzzer.search;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.utils.Tuple;

import java.util.List;

public abstract class Search {

    private final ASTNode nodeToSample;

    private final Long timeBudgetMilis;

    private final Context rootContext;

    private final Long seed;

    public Search(ASTNode nodeToSample, Long timeBudgetMilis,
                  Context rootContext, Long seed) {
        this.nodeToSample = nodeToSample;
        this.timeBudgetMilis = timeBudgetMilis;
        this.rootContext = rootContext;
        this.seed = seed;
    }

    public ASTNode getNodeToSample() {
        return nodeToSample;
    }

    public Long getTimeBudgetMilis() {
        return timeBudgetMilis;
    }

    public Context getRootContext() {
        return rootContext;
    }

    public Long getSeed() {
        return seed;
    }

    public abstract List<Tuple<CodeFragment, FuzzerStatistics>> search();
}
