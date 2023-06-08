package org.fuzzer.search.operators.generator;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;

import java.util.LinkedList;
import java.util.List;

public class SuiteGenerator {

    private final BlockGenerator blockGenerator;

    public SuiteGenerator(Context rootContext, Long seed) {
        this.blockGenerator = new BlockGenerator(rootContext, seed);
    }

    public List<TestSuite> generateSuites(long numberOfSuites, long blocksInSuite,
                                          ASTNode nodeToSample, FuzzerStatistics globalStats) {
        List<TestSuite> suites = new LinkedList<>();

        while (suites.size() < numberOfSuites) {
            List<CodeBlock> blocks = blockGenerator.generateBlocks(blocksInSuite, nodeToSample, globalStats);
            suites.add(new TestSuite(blocks));
        }

        return suites;
    }
}
